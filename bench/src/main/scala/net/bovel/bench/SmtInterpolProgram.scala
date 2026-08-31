package net.bovel.bench

import scala.collection.mutable.{ArrayBuffer, HashMap}

import de.uni_freiburg.informatik.ultimate.logic.{Script as ItpScript, Sort as ItpSort, Term as ItpTerm}
import de.uni_freiburg.informatik.ultimate.smtinterpol.DefaultLogger
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol
import net.bovel.smt2.{Command, Script}
import net.bovel.vik.{Datatype, Formula, Term}

/** An instance compiled to a straight-line program of SMTInterpol API calls —
 *  the `Script` interface Ultimate embeds — the SMTInterpol analogue of
 *  [[Z3Program]]: declarations, term construction, asserts, `push`/`pop` and
 *  every `check-sat`, replayed against a fresh solver per [[check]]. `Int`
 *  covers the lowered bit-vectors.
 */
final class SmtInterpolProgram(instructions: Array[SmtInterpolProgram.Op], slotCount: Int, sortCount: Int):
  import SmtInterpolProgram.Op

  // SMT-LIB pipe quoting is not part of the symbol: SMTInterpol wants the bare name and quotes itself.
  private def unquote(name: String): String =
    if name.length >= 2 && name.head == '|' && name.last == '|' then name.substring(1, name.length - 1) else name

  /** Replays the program and returns the `check-sat` answers. */
  def check(): List[Boolean] =
    val s = SMTInterpol(DefaultLogger())
    s.setOption(":verbosity", 2)
    val slots = new Array[ItpTerm](slotCount)
    val sorts = new Array[ItpSort](sortCount)
    val answers = ArrayBuffer.empty[Boolean]
    for op <- instructions do
      op match
        case Op.SetLogic(logic) => s.setLogic(logic)
        case Op.MkSort(id, name) => sorts(id) = s.sort(unquote(name))
        case Op.DeclareSort(id, name) =>
          s.declareSort(unquote(name), 0)
          sorts(id) = s.sort(unquote(name))
        case Op.DeclareAdts(datatypes, sortIds) =>
          val dts = datatypes.map(d => s.datatype(unquote(d.name), 0))
          for (d, i) <- datatypes.zipWithIndex do sorts(sortIds(d.name)) = s.sort(unquote(d.name))
          val ctorss = datatypes.map { d =>
            d.constructors.map { c =>
              s.constructor(unquote(c.name), c.selectors.map((s0, _) => unquote(s0)).toArray, c.selectors.map((_, t) => s.sort(unquote(t))).toArray)
            }.toArray
          }
          // A non-parametric datatype wants null for its sort parameters: an empty array means arity zero
          // was declared with parameters, which declareConstructorFunctions rejects.
          s.declareDatatypes(dts.toArray, ctorss.toArray, Array.fill[Array[ItpSort] | Null](datatypes.length)(null))
        case Op.DeclareFun(name, argSorts, result) => s.declareFun(unquote(name), argSorts.map(sorts), sorts(result))
        case Op.MkConst(dst, name) => slots(dst) = s.term(unquote(name))
        case Op.MkApp(dst, name, args) => slots(dst) = s.term(unquote(name), args.map(slots)*)
        case Op.MkIs(dst, ctor, arg) => slots(dst) = s.term("is", Array(unquote(ctor)), null, slots(arg))
        case Op.MkNum(dst, value) =>
          slots(dst) =
            if value.signum < 0 then s.term("-", s.numeral((-value).bigInteger)) else s.numeral(value.bigInteger)
        case Op.MkLinear(dst, coefficients, args, constant) =>
          def num(value: BigInt): ItpTerm =
            if value.signum < 0 then s.term("-", s.numeral((-value).bigInteger)) else s.numeral(value.bigInteger)
          val parts = ArrayBuffer.empty[ItpTerm]
          for (c, a) <- coefficients.zip(args) do
            parts += (if c == BigInt(1) then slots(a) else s.term("*", num(c), slots(a)))
          if constant.signum != 0 then parts += num(constant)
          slots(dst) = if parts.length == 1 then parts.head else s.term("+", parts.toSeq*)
        case Op.MkTrue(dst) => slots(dst) = s.term("true")
        case Op.MkFalse(dst) => slots(dst) = s.term("false")
        case Op.MkNot(dst, a) => slots(dst) = s.term("not", slots(a))
        case Op.MkAnd(dst, args) => slots(dst) = s.term("and", args.map(slots)*)
        case Op.MkOr(dst, args) => slots(dst) = s.term("or", args.map(slots)*)
        case Op.MkImplies(dst, a, b) => slots(dst) = s.term("=>", slots(a), slots(b))
        case Op.MkIff(dst, a, b) => slots(dst) = s.term("=", slots(a), slots(b))
        case Op.MkIte(dst, c, t, e) => slots(dst) = s.term("ite", slots(c), slots(t), slots(e))
        case Op.MkEq(dst, l, r) => slots(dst) = s.term("=", slots(l), slots(r))
        case Op.MkLe(dst, l, r) => slots(dst) = s.term("<=", slots(l), slots(r))
        case Op.MkLt(dst, l, r) => slots(dst) = s.term("<", slots(l), slots(r))
        case Op.Assert(slot) => s.assertTerm(slots(slot))
        case Op.Push(levels) => s.push(levels)
        case Op.Pop(levels) => s.pop(levels)
        case Op.Check => answers += (s.checkSat() == ItpScript.LBool.SAT)
    answers.toList

object SmtInterpolProgram:
  enum Op:
    case SetLogic(logic: String)
    case MkSort(id: Int, name: String)
    case DeclareSort(id: Int, name: String)
    case DeclareAdts(datatypes: List[Datatype], sortIds: Map[String, Int])
    case DeclareFun(name: String, argSorts: Array[Int], result: Int)
    case MkConst(dst: Int, name: String)
    case MkApp(dst: Int, name: String, args: Array[Int])
    case MkIs(dst: Int, ctor: String, arg: Int)
    case MkNum(dst: Int, value: BigInt)
    case MkLinear(dst: Int, coefficients: Array[BigInt], args: Array[Int], constant: BigInt)
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

  /** Compiles the script: the only walk of VIK's tree, done before measuring. */
  def compile(script: Script): SmtInterpolProgram =
    val out = ArrayBuffer.empty[Op]
    val sortIds = HashMap.empty[String, Int]
    val slots = HashMap.empty[Formula | Term, Int]
    val testers = HashMap.empty[String, String] // "(_ is C)" to C
    var slotCount = 0
    def fresh(): Int =
      slotCount += 1
      slotCount - 1
    def sortId(name: String): Int = sortIds.getOrElseUpdate(
      name, {
        val id = sortIds.size
        name match
          case "Int" | "Bool" => out += Op.MkSort(id, name)
          case _ => out += Op.DeclareSort(id, name)
        id
      }
    )
    def term(t: Term): Int = t match
      case Term.Bool(f) => formula(f)
      case _ =>
        slots.getOrElseUpdate(
          t, {
            val dst = fresh()
            t match
              case Term.Const(name) => out += Op.MkConst(dst, name)
              case Term.App(Term.Const(name), as) =>
                testers.get(name) match
                  case Some(ctor) => out += Op.MkIs(dst, ctor, term(as.head))
                  case None => out += Op.MkApp(dst, name, as.map(term).toArray)
              case Term.App(fun, _) => throw IllegalArgumentException(s"higher-order application $fun")
              case Term.Num(v) => out += Op.MkNum(dst, v)
              case Term.Linear(terms, c) =>
                out += Op.MkLinear(dst, terms.map(_._1).toArray, terms.map((_, u) => term(u)).toArray, c)
              case Term.Lambda(_, _) | Term.Var(_) =>
                throw UnsupportedOperationException("lambdas are not encoded for SMTInterpol")
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
        case Command.SetLogic(logic) => out += Op.SetLogic(logic)
        case Command.DeclareSort(name, _) => sortId(name)
        case Command.DeclareFun(name, args, result) =>
          out += Op.DeclareFun(name, args.map(sortId).toArray, sortId(result))
        case Command.DeclareDatatypes(datatypes) =>
          // The datatype names take sort ids silently: DeclareAdts fills them, no declare-sort op.
          for d <- datatypes do if !sortIds.contains(d.name) then sortIds(d.name) = sortIds.size
          for d <- datatypes; c <- d.constructors do
            testers(c.tester) = c.name
            for (_, s) <- c.selectors if !datatypes.exists(_.name == s) do sortId(s)
          out += Op.DeclareAdts(datatypes, sortIds.toMap)
        case Command.Assert(f) => out += Op.Assert(formula(f))
        case Command.AssertNamed(_, f) => out += Op.Assert(formula(f))
        case Command.Push(levels) => out += Op.Push(levels)
        case Command.Pop(levels) => out += Op.Pop(levels)
        case Command.CheckSat => out += Op.Check
        case _ => ()
    SmtInterpolProgram(out.toArray, slotCount, sortIds.size)
