package net.bovel.bench

import scala.collection.mutable.ArrayBuffer

import net.bovel.smtlib.{Command, Formula, Script, Sort, Term}
import net.bovel.vik.Session

/** Runs a [[Replay]] through VIK's [[Session]] — the API a tool that embeds the
 *  solver calls with the trees of `net.bovel.smtlib` — against a fresh session:
 *  declarations, term construction, asserts, `push`/`pop` and every
 *  `check-sat`, nothing else. The trees are the ones VIK's parser builds from
 *  the same text — a Boolean equality is an `Iff`, a Boolean-sorted term in a
 *  formula position a `Pred`, a formula in a term position a `Bool` — so the
 *  session lowers exactly what it would have parsed, and the lowering is part
 *  of the operation, as it is of every call through this API.
 */
object VikReplay:
  import Replay.Op

  /** Replays the program and returns the `check-sat` answers. */
  def check(program: Replay): List[Boolean] =
    val s = Session()
    val slots = new Array[Term | Formula](program.slotCount)
    val sorts = new Array[Sort](program.sortCount)
    val names = new Array[String](program.declCount)
    val answers = ArrayBuffer.empty[Boolean]
    def term(i: Int): Term = slots(i) match
      case t: Term => t
      case f: Formula => Term.Bool(f)
    def formula(i: Int): Formula = slots(i) match
      case f: Formula => f
      case t: Term => Formula.Pred(t)
    def terms(is: Array[Int]): List[Term] = is.toList.map(term)
    def formulas(is: Array[Int]): List[Formula] = is.toList.map(formula)
    def boolean(i: Int): Boolean = program.slotSorts(i) == Sort.Bool
    def run(command: Command): Unit = s.run(Script(List(command)))
    if program.logic != null then run(Command.SetLogic(program.logic))
    for op <- program.ops do
      op match
        case Op.MkSort(id, sort) => sorts(id) = sort
        case Op.ArraySort(id, index, element) => sorts(id) = Sort.Array(sorts(index), sorts(element))
        case Op.DeclareSort(id, name) =>
          sorts(id) = Sort.Named(name)
          run(Command.DeclareSort(name, 0))
        case Op.DeclareDatatypes(datatypes, sortIds, ctorIds, selectorIds) =>
          for (sort, id) <- sortIds do sorts(id) = sort
          for d <- datatypes; c <- d.constructors do
            names(ctorIds(c.name)) = c.name
            for (selector, _) <- c.selectors do names(selectorIds(selector)) = selector
          run(Command.DeclareDatatypes(datatypes))
        case Op.DeclareFun(id, name, argSorts, result) =>
          names(id) = name
          run(Command.DeclareFun(name, argSorts.toList.map(sorts), sorts(result)))
        case Op.Const(dst, decl) => slots(dst) = Term.Const(names(decl))
        case Op.App(dst, decl, args) => slots(dst) = Term.App(Term.Const(names(decl)), terms(args))
        case Op.Numeral(dst, value, _) => slots(dst) = Term.Numeral(value)
        case Op.Decimal(dst, unscaled, scale) => slots(dst) = Term.Decimal(unscaled, scale)
        case Op.Bits(dst, value, width) => slots(dst) = Term.Bits(value, width)
        case Op.Arith(dst, aop, _, args) => slots(dst) = Term.ArithApp(aop, terms(args))
        case Op.Bv(dst, bop, args) => slots(dst) = Term.BvApp(bop, terms(args))
        case Op.Select(dst, _, array, index) => slots(dst) = Term.Select(term(array), term(index))
        case Op.Store(dst, _, array, index, value) =>
          slots(dst) = Term.Store(term(array), term(index), term(value))
        case Op.Ite(dst, c, t, e) =>
          slots(dst) =
            if boolean(dst) then Formula.Ite(formula(c), formula(t), formula(e))
            else Term.Ite(formula(c), term(t), term(e))
        case Op.True(dst) => slots(dst) = Formula.True
        case Op.False(dst) => slots(dst) = Formula.False
        case Op.Not(dst, a) => slots(dst) = Formula.Not(formula(a))
        case Op.And(dst, args) => slots(dst) = Formula.And(formulas(args))
        case Op.Or(dst, args) => slots(dst) = Formula.Or(formulas(args))
        case Op.Implies(dst, a, b) => slots(dst) = Formula.Implies(List(formula(a), formula(b)))
        case Op.Xor(dst, a, b) => slots(dst) = Formula.Xor(List(formula(a), formula(b)))
        case Op.Iff(dst, a, b) => slots(dst) = Formula.Iff(List(formula(a), formula(b)))
        case Op.Eq(dst, args) =>
          slots(dst) = if boolean(args(0)) then Formula.Iff(formulas(args)) else Formula.Eq(terms(args))
        case Op.Distinct(dst, args) => slots(dst) = Formula.Distinct(terms(args))
        case Op.Cmp(dst, cop, _, args) => slots(dst) = Formula.Cmp(cop, terms(args))
        case Op.BvCmp(dst, cop, l, r) => slots(dst) = Formula.BvCmp(cop, term(l), term(r))
        case Op.IsCtor(dst, ctor, arg) => slots(dst) = Formula.IsConstructor(names(ctor), term(arg))
        case Op.IsInt(dst, a) => slots(dst) = Formula.IsInt(term(a))
        case Op.Param(dst, name, _) => slots(dst) = Term.Const(name)
        case Op.Lambda(dst, params, body) => slots(dst) = Term.Lambda(params, term(body))
        case Op.Assert(slot) => s.assert(formula(slot))
        case Op.Push(levels) => for _ <- 1 to levels do s.push()
        case Op.Pop(levels) => for _ <- 1 to levels do s.pop()
        case Op.Check => answers += (s.checkSat() == Some(true))
    answers.toList
