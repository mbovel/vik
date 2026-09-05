package io.bovel.bench.evaluation

import scala.collection.mutable.ArrayBuffer

import de.uni_freiburg.informatik.ultimate.logic.{Script as ItpScript, Sort as ItpSort, Term as ItpTerm}
import de.uni_freiburg.informatik.ultimate.smtinterpol.DefaultLogger
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.SMTInterpol
import io.bovel.smtlib.{ArithOp, BvOp}
import io.bovel.smtlib.Sort as SmtSort

/** Runs a [[Replay]] through SMTInterpol's `Script` interface — the one
 *  Ultimate embeds — against a fresh solver: declarations, term construction,
 *  asserts, `push`/`pop` and every `check-sat`, nothing else. The interface
 *  takes SMT-LIB's own names and is n-ary where the standard is, so an
 *  operation is one `term` call.
 */
object SmtInterpolReplay:
  import Replay.Op

  // SMT-LIB pipe quoting is not part of the symbol: SMTInterpol wants the bare name and quotes itself.
  private def unquote(name: String): String =
    if name.length >= 2 && name.head == '|' && name.last == '|' then name.substring(1, name.length - 1) else name

  /** Replays the program and returns the `check-sat` answers. */
  def check(program: Replay): List[Boolean] =
    val s = SMTInterpol(DefaultLogger())
    s.setOption(":verbosity", 2)
    if program.resets then s.setOption(":global-declarations", true) // a reset keeps the declarations
    if program.logic != null then s.setLogic(program.logic)
    val slots = new Array[ItpTerm](program.slotCount)
    val sorts = new Array[ItpSort](program.sortCount)
    val names = new Array[String](program.declCount)
    val answers = ArrayBuffer.empty[Boolean]
    def args(is: Array[Int]): Array[ItpTerm] = is.map(slots)
    def numeral(value: BigInt): ItpTerm =
      if value.signum < 0 then s.term("-", s.numeral((-value).bigInteger)) else s.numeral(value.bigInteger)
    for op <- program.ops do
      op match
        case Op.MkSort(id, sort) =>
          sorts(id) = sort match
            case SmtSort.BitVec(width) => s.sort("BitVec", Array(width.toString))
            case _ => s.sort(sort.text)
        case Op.ArraySort(id, index, element) => sorts(id) = s.sort("Array", sorts(index), sorts(element))
        case Op.DeclareSort(id, name) =>
          s.declareSort(unquote(name), 0)
          sorts(id) = s.sort(unquote(name))
        case Op.DeclareDatatypes(datatypes, sortIds, ctorIds, selectorIds) =>
          val dts = datatypes.map(d => s.datatype(unquote(d.name), 0))
          for (d, i) <- datatypes.zipWithIndex do sorts(sortIds(SmtSort.Named(d.name))) = s.sort(unquote(d.name))
          val ctorss = datatypes.map { d =>
            d.constructors.map { c =>
              val fieldSorts = c.selectors.map((_, t) => sortIds.get(t).map(sorts).getOrElse(s.sort(unquote(t.text))))
              s.constructor(unquote(c.name), c.selectors.map((s0, _) => unquote(s0)).toArray, fieldSorts.toArray)
            }.toArray
          }
          // A non-parametric datatype wants null for its sort parameters: an empty array means arity zero
          // was declared with parameters, which declareConstructorFunctions rejects.
          s.declareDatatypes(dts.toArray, ctorss.toArray, Array.fill[Array[ItpSort] | Null](datatypes.length)(null))
          for d <- datatypes; c <- d.constructors do
            names(ctorIds(c.name)) = unquote(c.name)
            for (selector, _) <- c.selectors do names(selectorIds(selector)) = unquote(selector)
        case Op.DeclareFun(id, name, argSorts, result) =>
          names(id) = unquote(name)
          s.declareFun(names(id), argSorts.map(sorts), sorts(result))
        case Op.Const(dst, decl) => slots(dst) = s.term(names(decl))
        case Op.App(dst, decl, as) => slots(dst) = s.term(names(decl), args(as)*)
        case Op.Numeral(dst, value, real) =>
          slots(dst) = if real then s.decimal(java.math.BigDecimal(value.bigInteger)) else numeral(value)
        case Op.Decimal(dst, unscaled, scale) =>
          slots(dst) = s.decimal(java.math.BigDecimal(unscaled.bigInteger, scale))
        case Op.Bits(dst, value, width) =>
          val bits = value.toString(2)
          slots(dst) = s.binary("#b" + "0" * (width - bits.length) + bits)
        case Op.Arith(dst, aop, _, as) => slots(dst) = s.term(aop.name, args(as)*)
        case Op.Bv(dst, BvOp.Concat, as) if as.length > 2 => // binary for SMTInterpol, left to right
          slots(dst) = args(as).reduceLeft((l, r) => s.term("concat", l, r))
        case Op.Bv(dst, bop, as) =>
          slots(dst) = bop.indices match
            case Nil => s.term(bop.name, args(as)*)
            case indices => s.term(bop.name, indices.map(_.toString).toArray, null, args(as)*)
        case Op.Select(dst, _, array, index) => slots(dst) = s.term("select", slots(array), slots(index))
        case Op.Store(dst, _, array, index, value) =>
          slots(dst) = s.term("store", slots(array), slots(index), slots(value))
        case Op.Ite(dst, c, t, e) => slots(dst) = s.term("ite", slots(c), slots(t), slots(e))
        case Op.True(dst) => slots(dst) = s.term("true")
        case Op.False(dst) => slots(dst) = s.term("false")
        case Op.Not(dst, a) => slots(dst) = s.term("not", slots(a))
        case Op.And(dst, as) => slots(dst) = s.term("and", args(as)*)
        case Op.Or(dst, as) => slots(dst) = s.term("or", args(as)*)
        case Op.Implies(dst, a, b) => slots(dst) = s.term("=>", slots(a), slots(b))
        case Op.Xor(dst, a, b) => slots(dst) = s.term("xor", slots(a), slots(b))
        case Op.Iff(dst, a, b) => slots(dst) = s.term("=", slots(a), slots(b))
        case Op.Eq(dst, as) => slots(dst) = s.term("=", args(as)*)
        case Op.Distinct(dst, as) => slots(dst) = s.term("distinct", args(as)*)
        case Op.Cmp(dst, cop, _, as) => slots(dst) = s.term(cop.name, args(as)*)
        case Op.BvCmp(dst, cop, l, r) => slots(dst) = s.term(cop.name, slots(l), slots(r))
        case Op.IsCtor(dst, ctor, arg) => slots(dst) = s.term("is", Array(names(ctor)), null, slots(arg))
        case Op.IsInt(dst, a) => slots(dst) = s.term("is_int", slots(a))
        case Op.Assert(slot) => s.assertTerm(slots(slot))
        case Op.Push(levels) => s.push(levels)
        case Op.Pop(levels) => s.pop(levels)
        case Op.Reset => s.resetAssertions()
        case Op.ResetAll =>
          s.reset() // the options and the logic with it: set again, as at the start
          s.setOption(":verbosity", 2)
          s.setOption(":global-declarations", true)
          if program.logic != null then s.setLogic(program.logic)
        case Op.Check =>
          answers += (s.checkSat() match
            case ItpScript.LBool.SAT => true
            case ItpScript.LBool.UNSAT => false
            case answer => throw Replay.Undecided("SMTInterpol", answer.toString))
    answers.toList
