package net.bovel.bench

import scala.collection.mutable.{ArrayBuffer, HashMap}

import com.microsoft.z3.{BoolExpr, Context, Expr, FuncDecl, Sort}
import net.bovel.smt2.{Command, Script}
import net.bovel.smt2.{Datatype, Formula, Term}

/** An instance compiled to a straight-line program of z3 Java API calls: what
 *  a verifier that builds z3's terms directly executes per query. [[compile]]
 *  reads the parsed script once, up front; [[run]] — the measured operation —
 *  replays the program against a fresh context, so it pays declarations, term
 *  construction and nothing else: no VIK tree is walked, no text is parsed.
 *  Every distinct subterm is built once and kept in a slot, the way a
 *  builder's cache would; sorts and declarations are interned to indices.
 *  `Int` covers the lowered bit-vectors, as the front end lowers while
 *  parsing.
 */
final class Z3Program(instructions: Array[Z3Program.Op], slotCount: Int, declCount: Int, sortCount: Int):
  import Z3Program.Op

  /** Replays the program in `ctx` against a fresh solver — asserts, `push`,
   *  `pop` and `check-sat` in script order — and returns the answers.
   */
  def check(ctx: Context): List[Boolean] =
    val solver = ctx.mkSolver()
    val slots = new Array[Expr[?]](slotCount)
    val decls = new Array[FuncDecl[?]](declCount)
    val sorts = new Array[Sort](sortCount)
    val answers = ArrayBuffer.empty[Boolean]
    def bool(slot: Int): BoolExpr = slots(slot).asInstanceOf[BoolExpr]
    def args(slots0: Array[Int]): Array[Expr[?]] = slots0.map(slots)
    // Real arithmetic: an integer numeral or coefficient in a real position is coerced, so the same
    // slot serves integer and real uses (the tree's literals are shared).
    def real(e: Expr[?]): Boolean = e.getSort == ctx.getRealSort
    def arith(e: Expr[?]): Expr[com.microsoft.z3.ArithSort] = e.asInstanceOf[Expr[com.microsoft.z3.ArithSort]]
    def lift(e: Expr[?]): Expr[com.microsoft.z3.ArithSort] =
      if real(e) then arith(e)
      else ctx.mkInt2Real(e.asInstanceOf[com.microsoft.z3.IntExpr]).asInstanceOf[Expr[com.microsoft.z3.ArithSort]]
    def pair(l: Int, r: Int): (Expr[com.microsoft.z3.ArithSort], Expr[com.microsoft.z3.ArithSort]) =
      val (a, b) = (slots(l), slots(r))
      if real(a) || real(b) then (lift(a), lift(b)) else (arith(a), arith(b))
    for op <- instructions do
      op match
        case Op.UninterpretedSort(id, name) => sorts(id) = ctx.mkUninterpretedSort(name)
        case Op.BoolSort(id) => sorts(id) = ctx.getBoolSort
        case Op.IntSort(id) => sorts(id) = ctx.getIntSort
        case Op.RealSort(id) => sorts(id) = ctx.getRealSort
        case Op.DeclareFun(id, name, argSorts, result) =>
          decls(id) = ctx.mkFuncDecl(name, argSorts.map(sorts), sorts(result))
        case Op.DeclareDatatypes(datatypes, sortIds, declIds) =>
          // Mutually recursive datatypes: field sorts naming a datatype of the group are references by index.
          val names = datatypes.map(_.name)
          val constructors = datatypes.map { d =>
            d.constructors.map { c =>
              val fieldSorts = c.selectors.map((_, s) => if names.contains(s) then null else sorts(sortIds(s)))
              val refs = c.selectors.map((_, s) => names.indexOf(s))
              ctx.mkConstructor(c.name, c.tester, c.selectors.map(_._1).toArray, fieldSorts.toArray, refs.toArray)
            }.toArray
          }
          val created = ctx.mkDatatypeSorts(names.toArray, constructors.toArray)
          for (d, i) <- datatypes.zipWithIndex do
            sorts(sortIds(d.name)) = created(i)
            for (c, k) <- d.constructors.zipWithIndex do
              val constructor = constructors(i)(k)
              decls(declIds(c.name)) = constructor.ConstructorDecl()
              decls(declIds(c.tester)) = constructor.getTesterDecl
              for ((selector, _), j) <- c.selectors.zipWithIndex do
                decls(declIds(selector)) = constructor.getAccessorDecls()(j)
        case Op.MkConst(dst, decl) => slots(dst) = ctx.mkConst(decls(decl))
        case Op.MkApp(dst, decl, as) => slots(dst) = ctx.mkApp(decls(decl), args(as)*)
        case Op.MkNum(dst, value) => slots(dst) = ctx.mkInt(value)
        case Op.MkMul(dst, coefficient, a) =>
          val arg = slots(a)
          val c: Expr[com.microsoft.z3.ArithSort] =
            if real(arg) then arith(ctx.mkReal(coefficient)) else arith(ctx.mkInt(coefficient))
          slots(dst) = ctx.mkMul(c, arith(arg))
        case Op.MkAdd(dst, as) =>
          val xs = args(as)
          slots(dst) = if xs.exists(real) then ctx.mkAdd(xs.map(lift)*) else ctx.mkAdd(xs.map(arith)*)
        case Op.MkTrue(dst) => slots(dst) = ctx.mkTrue()
        case Op.MkFalse(dst) => slots(dst) = ctx.mkFalse()
        case Op.MkNot(dst, a) => slots(dst) = ctx.mkNot(bool(a))
        case Op.MkAnd(dst, as) => slots(dst) = ctx.mkAnd(as.map(bool)*)
        case Op.MkOr(dst, as) => slots(dst) = ctx.mkOr(as.map(bool)*)
        case Op.MkImplies(dst, a, b) => slots(dst) = ctx.mkImplies(bool(a), bool(b))
        case Op.MkIff(dst, a, b) => slots(dst) = ctx.mkIff(bool(a), bool(b))
        case Op.MkIte(dst, c, t, e) => slots(dst) = ctx.mkITE(bool(c), bool(t), bool(e))
        case Op.MkEq(dst, l, r) =>
          slots(dst) =
            if real(slots(l)) || real(slots(r)) then { val (a, b) = pair(l, r); ctx.mkEq(a, b) }
            else ctx.mkEq(slots(l), slots(r))
        case Op.MkLe(dst, l, r) =>
          val (a, b) = pair(l, r)
          slots(dst) = ctx.mkLe(a, b)
        case Op.MkLt(dst, l, r) =>
          val (a, b) = pair(l, r)
          slots(dst) = ctx.mkLt(a, b)
        case Op.Assert(slot) => solver.add(bool(slot))
        case Op.Push(levels) => for _ <- 1 to levels do solver.push()
        case Op.Pop(levels) => solver.pop(levels)
        case Op.Check => answers += (solver.check() == com.microsoft.z3.Status.SATISFIABLE)
    answers.toList

object Z3Program:
  enum Op:
    case UninterpretedSort(id: Int, name: String)
    case BoolSort(id: Int)
    case IntSort(id: Int)
    case RealSort(id: Int)
    case DeclareFun(id: Int, name: String, argSorts: Array[Int], result: Int)
    case DeclareDatatypes(datatypes: List[Datatype], sortIds: Map[String, Int], declIds: Map[String, Int])
    case MkConst(dst: Int, decl: Int)
    case MkApp(dst: Int, decl: Int, args: Array[Int])
    case MkNum(dst: Int, value: String)
    case MkMul(dst: Int, coefficient: String, a: Int)
    case MkAdd(dst: Int, args: Array[Int])
    case MkTrue(dst: Int)
    case MkFalse(dst: Int)
    case MkNot(dst: Int, a: Int)
    case MkAnd(dst: Int, args: Array[Int])
    case MkOr(dst: Int, args: Array[Int])
    case MkImplies(dst: Int, a: Int, b: Int)
    case MkIff(dst: Int, a: Int, b: Int)
    case MkIte(dst: Int, c: Int, t: Int, e: Int)
    case MkEq(dst: Int, l: Int, r: Int)
    case MkLe(dst: Int, l: Int, r: Int)
    case MkLt(dst: Int, l: Int, r: Int)
    case Assert(slot: Int)
    case Push(levels: Int)
    case Pop(levels: Int)
    case Check

  /** Compiles the script: the only walk of VIK's tree, done before measuring.
   */
  def compile(script: Script): Z3Program =
    val out = ArrayBuffer.empty[Op]
    val sortIds = HashMap.empty[String, Int]
    val declIds = HashMap.empty[String, Int]
    val slots = HashMap.empty[Formula | Term, Int]
    var slotCount = 0
    def fresh(): Int =
      slotCount += 1
      slotCount - 1
    def sortId(name: String): Int = sortIds.getOrElseUpdate(
      name, {
        val id = sortIds.size
        name match
          case "Bool" => out += Op.BoolSort(id)
          case "Int" => out += Op.IntSort(id)
          case "Real" => out += Op.RealSort(id)
          case _ => out += Op.UninterpretedSort(id, name)
        id
      }
    )
    def declId(name: String): Int = declIds.getOrElseUpdate(name, declIds.size)
    def term(t: Term): Int = t match
      case Term.Bool(f) => formula(f) // a formula as a term: the same slot
      case _ =>
        slots.getOrElseUpdate(
          t, {
            val dst = fresh()
            t match
              case Term.Const(name) => out += Op.MkConst(dst, declId(name))
              case Term.App(Term.Const(name), as) => out += Op.MkApp(dst, declId(name), as.map(term).toArray)
              case Term.App(fun, _) => throw IllegalArgumentException(s"higher-order application $fun")
              case Term.Num(v) => out += Op.MkNum(dst, v.toString)
              case Term.Linear(terms, c) =>
                val summands = terms.map { (a, u) =>
                  if a == 1 then term(u)
                  else
                    val product = fresh()
                    out += Op.MkMul(product, a.toString, term(u))
                    product
                } ++ Option.when(c.signum != 0) {
                  val constant = fresh()
                  out += Op.MkNum(constant, c.toString)
                  constant
                }
                out += Op.MkAdd(dst, summands.toArray)
              case Term.Lambda(_, _) | Term.Var(_) =>
                throw UnsupportedOperationException("lambdas are not encoded for z3")
              case Term.Bool(_) => throw IllegalStateException("handled above")
            dst
          }
        )
    def formula(f: Formula): Int = slots.getOrElseUpdate(
      f, {
        f match
          case Formula.Pred(t) => term(t)
          case _ =>
            val dst = fresh()
            f match
              case Formula.True => out += Op.MkTrue(dst)
              case Formula.False => out += Op.MkFalse(dst)
              case Formula.Not(a) => out += Op.MkNot(dst, formula(a))
              case Formula.And(as) => out += Op.MkAnd(dst, as.map(formula).toArray)
              case Formula.Or(as) => out += Op.MkOr(dst, as.map(formula).toArray)
              case Formula.Implies(a, b) => out += Op.MkImplies(dst, formula(a), formula(b))
              case Formula.Iff(a, b) => out += Op.MkIff(dst, formula(a), formula(b))
              case Formula.Ite(c, t, e) => out += Op.MkIte(dst, formula(c), formula(t), formula(e))
              case Formula.Eq(l, r) => out += Op.MkEq(dst, term(l), term(r))
              case Formula.Le(l, r) => out += Op.MkLe(dst, term(l), term(r))
              case Formula.Less(l, r) => out += Op.MkLt(dst, term(l), term(r))
              case Formula.Pred(_) => throw IllegalStateException("handled above")
            dst
      }
    )
    for command <- script.commands do
      command match
        case Command.DeclareSort(name, _) => sortId(name)
        case Command.DeclareFun(name, args, result) =>
          out += Op.DeclareFun(declId(name), name, args.map(sortId).toArray, sortId(result))
        case Command.DeclareDatatypes(datatypes) =>
          val names = datatypes.map(d => d.name -> sortId(d.name)).toMap
          val members = datatypes.flatMap(d =>
            d.constructors.flatMap(c => (c.name :: c.tester :: c.selectors.map(_._1)).map(n => n -> declId(n)))
          ).toMap
          for d <- datatypes; c <- d.constructors; (_, s) <- c.selectors if !names.contains(s) do sortId(s)
          out += Op.DeclareDatatypes(datatypes, names ++ datatypes.flatMap(d =>
            d.constructors.flatMap(c => c.selectors.map((_, s) => s -> sortIds(s)))
          ), members)
        case Command.Assert(f) => out += Op.Assert(formula(f))
        case Command.AssertNamed(_, f) => out += Op.Assert(formula(f)) // the name changes nothing z3 sees
        case Command.Push(levels) => out += Op.Push(levels)
        case Command.Pop(levels) => out += Op.Pop(levels)
        case Command.CheckSat => out += Op.Check
        case _ => () // check-sat-assuming, get-*, set-*: not part of what z3 is measured on
    Z3Program(out.toArray, slotCount, declIds.size, sortIds.size)
