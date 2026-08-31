package net.bovel.bench

import scala.collection.mutable.{ArrayBuffer, HashMap}

import com.microsoft.z3.{BoolExpr, Context, Expr, FuncDecl, Sort}
import net.bovel.smt2.{Command, Script}
import net.bovel.vik.{Datatype, Formula, Term}

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

  /** Replays the program in `ctx` and returns the asserted formulas. */
  def run(ctx: Context): Array[BoolExpr] =
    val slots = new Array[Expr[?]](slotCount)
    val decls = new Array[FuncDecl[?]](declCount)
    val sorts = new Array[Sort](sortCount)
    val asserted = ArrayBuffer.empty[BoolExpr]
    def bool(slot: Int): BoolExpr = slots(slot).asInstanceOf[BoolExpr]
    def args(slots0: Array[Int]): Array[Expr[?]] = slots0.map(slots)
    for op <- instructions do
      op match
        case Op.UninterpretedSort(id, name) => sorts(id) = ctx.mkUninterpretedSort(name)
        case Op.BoolSort(id) => sorts(id) = ctx.getBoolSort
        case Op.IntSort(id) => sorts(id) = ctx.getIntSort
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
          slots(dst) = ctx.mkMul(ctx.mkInt(coefficient), slots(a).asInstanceOf[Expr[com.microsoft.z3.ArithSort]])
        case Op.MkAdd(dst, as) =>
          slots(dst) = ctx.mkAdd(args(as).map(_.asInstanceOf[Expr[com.microsoft.z3.ArithSort]])*)
        case Op.MkTrue(dst) => slots(dst) = ctx.mkTrue()
        case Op.MkFalse(dst) => slots(dst) = ctx.mkFalse()
        case Op.MkNot(dst, a) => slots(dst) = ctx.mkNot(bool(a))
        case Op.MkAnd(dst, as) => slots(dst) = ctx.mkAnd(as.map(bool)*)
        case Op.MkOr(dst, as) => slots(dst) = ctx.mkOr(as.map(bool)*)
        case Op.MkImplies(dst, a, b) => slots(dst) = ctx.mkImplies(bool(a), bool(b))
        case Op.MkIff(dst, a, b) => slots(dst) = ctx.mkIff(bool(a), bool(b))
        case Op.MkIte(dst, c, t, e) => slots(dst) = ctx.mkITE(bool(c), bool(t), bool(e))
        case Op.MkEq(dst, l, r) => slots(dst) = ctx.mkEq(slots(l), slots(r))
        case Op.MkLe(dst, l, r) =>
          slots(dst) = ctx.mkLe(
            slots(l).asInstanceOf[Expr[com.microsoft.z3.ArithSort]],
            slots(r).asInstanceOf[Expr[com.microsoft.z3.ArithSort]]
          )
        case Op.Assert(slot) => asserted += bool(slot)
    asserted.toArray

object Z3Program:
  enum Op:
    case UninterpretedSort(id: Int, name: String)
    case BoolSort(id: Int)
    case IntSort(id: Int)
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
    case Assert(slot: Int)

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
        case _ => ()
    Z3Program(out.toArray, slotCount, declIds.size, sortIds.size)
