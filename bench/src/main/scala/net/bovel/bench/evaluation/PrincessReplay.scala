package net.bovel.bench.evaluation

import scala.collection.mutable.ArrayBuffer

import ap.api.SimpleAPI
import ap.basetypes.IdealInt
import ap.parser.{IExpression, IFormula, IFormulaITE, IFunApp, IFunction, ITerm, ITermITE}
import ap.theories.ADT
import ap.theories.arrays.ExtArray
import ap.theories.bitvectors.ModuloArithmetic
import ap.theories.rationals.Rationals
import ap.types.Sort
import net.bovel.smtlib.{ArithOp, BvCmpOp, BvOp, CmpOp}
import net.bovel.smtlib.Sort as SmtSort

/** Runs a [[Replay]] through princess's API against a fresh prover: symbol
 *  creation, term construction, asserts, `push`/`pop` and every `check-sat`,
 *  nothing else. The API route also sidesteps the SMT-LIB parser's environment,
 *  which rejects a re-declaration after `pop` (external-issues.md).
 *
 *  Sorts follow princess's own SMT-LIB parser: reals are the `Rationals`
 *  theory, bit-vectors `ModuloArithmetic`, arrays `ExtArray`; a `Bool` constant
 *  is a Boolean variable, a function with a `Bool` result a function into
 *  `MultipleValueBool` (0 is true) whose application is read as a formula by
 *  `= 0` — as a relation instead, princess pays up to 30× on the `kind` scripts
 *  (external-issues.md). A formula in a term's position is `ite(f, 0, 1)`.
 *
 *  The parser also flattens `and`, `or`, `+` and `*` written one inside another
 *  before folding them left, by a stack walk that keeps the first operand in
 *  place and reverses the rest at each level, and does not look through a name
 *  (`SMTParser2InputAbsy.flatten`). The replay does the same: with the operands
 *  in the script's order, the fifth `check-sat` of `kind__MESI_i4` takes 27 s
 *  instead of 0.2 s (external-issues.md).
 */
object PrincessReplay:
  import Replay.Op

  /** Replays the program and returns the `check-sat` answers. */
  def check(program: Replay): List[Boolean] =
    SimpleAPI.withProver { p =>
      import IExpression.*
      val slots = new Array[AnyRef](program.slotCount) // ITerm or IFormula
      val decls = new Array[AnyRef](program.declCount) // IFunction, or the constant's ITerm or IFormula
      val ctors = new Array[(ADT, Int)](program.declCount) // by the constructor's id, for its tester
      val sorts = new Array[Sort](program.sortCount)
      val arrays = new Array[ExtArray](program.sortCount) // by the array sort's id
      val answers = ArrayBuffer.empty[Boolean]
      def term(slot: Int): ITerm = slots(slot) match
        case t: ITerm => t
        case f: IFormula => ITermITE(f, i(0), i(1)) // 0 is princess's true
      def formula(slot: Int): IFormula = slots(slot) match
        case f: IFormula => f
        case t: ITerm => eqZero(t) // a Bool function application, read as a formula
      def terms(is: Array[Int]): Seq[ITerm] = is.toSeq.map(term)
      def pairs(is: Array[Int])(f: (ITerm, ITerm) => IFormula): IFormula = // a chainable operator
        if is.length == 2 then f(term(is(0)), term(is(1)))
        else and(is.zip(is.tail).map((l, r) => f(term(l), term(r))))
      def rational(value: BigInt): ITerm = Rationals.int2ring(i(IdealInt(value.bigInteger)))
      // The parser's flattening: the operands of a slot built by an operator, for that operator.
      val operands = new Array[(Any, Array[Int]) | Null](program.slotCount)
      def flatten(op: Any, args: Array[Int]): Seq[Int] =
        def spread(slot: Int): Seq[Int] =
          val built = operands(slot)
          if built != null && built._1 == op && !program.named(slot) then built._2.reverseIterator.flatMap(spread).toSeq
          else Seq(slot)
        args.iterator.flatMap(spread).toSeq
      for op <- program.ops do
        op match
          case Op.MkSort(id, sort) =>
            sorts(id) = sort match
              case SmtSort.Bool => Sort.MultipleValueBool // a Bool argument is a term here
              case SmtSort.Int => Sort.Integer
              case SmtSort.Real => Rationals.dom
              case SmtSort.BitVec(width) => ModuloArithmetic.UnsignedBVSort(width)
              case _ => throw IllegalStateException(s"not a built-in sort: $sort")
          case Op.ArraySort(id, index, element) =>
            arrays(id) = ExtArray(Seq(sorts(index)), sorts(element))
            sorts(id) = arrays(id).sort
          case Op.DeclareSort(id, name) => sorts(id) = Sort.createInfUninterpretedSort(name)
          case Op.DeclareDatatypes(datatypes, sortIds, ctorIds, selectorIds) =>
            val names = datatypes.map(_.name)
            val signatures = datatypes.map { d =>
              d.constructors.map { c =>
                val arguments = c.selectors.map { (selector, s) =>
                  val arg: ADT.CtorArgSort =
                    if names.contains(s.text) then ADT.ADTSort(names.indexOf(s.text))
                    else ADT.OtherSort(sorts(sortIds(s)))
                  (selector, arg)
                }
                (c.name, ADT.CtorSignature(arguments, ADT.ADTSort(names.indexOf(d.name))))
              }
            }
            val adt = new ADT(names, signatures.flatten)
            for (d, i) <- datatypes.zipWithIndex do sorts(sortIds(SmtSort.Named(d.name))) = adt.sorts(i)
            val constructors = datatypes.flatMap(_.constructors)
            for ((c, k), f) <- constructors.zipWithIndex.zip(adt.constructors) do
              decls(ctorIds(c.name)) = f
              ctors(ctorIds(c.name)) = (adt, k)
            for (c, fs) <- constructors.zip(adt.selectors); ((selector, _), f) <- c.selectors.zip(fs) do
              decls(selectorIds(selector)) = f
          case Op.DeclareFun(id, name, argSorts, result) =>
            decls(id) =
              if argSorts.nonEmpty then p.createFunction(name, argSorts.map(sorts).toSeq, sorts(result))
              else if sorts(result) == Sort.MultipleValueBool then p.createBooleanVariable(name)
              else p.createConstant(name, sorts(result))
          case Op.Const(dst, decl) =>
            slots(dst) = decls(decl) match
              case f: IFunction => IFunApp(f, Nil) // a nullary constructor
              case constant => constant
          case Op.App(dst, decl, as) => slots(dst) = IFunApp(decls(decl).asInstanceOf[IFunction], terms(as))
          case Op.Numeral(dst, value, real) =>
            slots(dst) = if real then rational(value) else i(IdealInt(value.bigInteger))
          case Op.Decimal(dst, unscaled, scale) =>
            slots(dst) = Rationals.divWithSpecialZero(rational(unscaled), rational(BigInt(10).pow(scale)))
          case Op.Bits(dst, value, width) => slots(dst) = ModuloArithmetic.bv(width, IdealInt(value.bigInteger))
          case Op.Arith(dst, aop, real, as) =>
            val ts = terms(as)
            operands(dst) = (aop, as)
            slots(dst) = aop match
              case ArithOp.Add =>
                val flat = flatten(aop, as).map(term)
                if real then Rationals.summation(flat*) else sum(flat)
              case ArithOp.Sub => if real then ts.reduceLeft[ITerm](Rationals.minus) else ts.reduceLeft[ITerm](_ - _)
              case ArithOp.Neg => if real then Rationals.minus(ts.head) else -ts.head
              case ArithOp.Mul =>
                val flat = flatten(aop, as).map(term)
                if real then flat.reduceLeft[ITerm](Rationals.mul) else flat.reduceLeft[ITerm](p.mulTheory.mult)
              case ArithOp.Div => Rationals.divWithSpecialZero(ts(0), ts(1))
              case ArithOp.IntDiv => p.mulTheory.eDivWithSpecialZero(ts(0), ts(1))
              case ArithOp.Mod => p.mulTheory.eModWithSpecialZero(ts(0), ts(1))
              case ArithOp.Abs => abs(ts.head)
              case ArithOp.ToReal => Rationals.int2ring(ts.head)
              case ArithOp.ToInt => Rationals.ring2int(ts.head)
          case Op.Bv(dst, bop, as) =>
            val ts = terms(as)
            def fold(f: (ITerm, ITerm) => ITerm): ITerm = ts.reduceLeft[ITerm](f)
            slots(dst) = bop match
              case BvOp.Add => fold(ModuloArithmetic.bvadd)
              case BvOp.Sub => fold(ModuloArithmetic.bvsub)
              case BvOp.Neg => ModuloArithmetic.bvneg(ts.head)
              case BvOp.Not => ModuloArithmetic.bvnot(ts.head)
              case BvOp.Mul => fold(ModuloArithmetic.bvmul)
              case BvOp.And => fold(ModuloArithmetic.bvand)
              case BvOp.Or => fold(ModuloArithmetic.bvor)
              case BvOp.Xor => fold(ModuloArithmetic.bvxor)
              case BvOp.Comp => ModuloArithmetic.bvcomp(ts(0), ts(1))
              case BvOp.Shl => ModuloArithmetic.bvshl(ts(0), ts(1))
              case BvOp.Lshr => ModuloArithmetic.bvlshr(ts(0), ts(1))
              case BvOp.Ashr => ModuloArithmetic.bvashr(ts(0), ts(1))
              case BvOp.Udiv => ModuloArithmetic.bvudiv(ts(0), ts(1))
              case BvOp.Sdiv => ModuloArithmetic.bvsdiv(ts(0), ts(1))
              case BvOp.Urem => ModuloArithmetic.bvurem(ts(0), ts(1))
              case BvOp.Srem => ModuloArithmetic.bvsrem(ts(0), ts(1))
              case BvOp.Smod => ModuloArithmetic.bvsmod(ts(0), ts(1))
              case BvOp.Concat => fold(ModuloArithmetic.concat)
              case BvOp.Extract(i, j) => ModuloArithmetic.extract(i, j, ts.head)
              case BvOp.ZeroExtend(k) => ModuloArithmetic.zero_extend(k, ts.head)
              case BvOp.SignExtend(k) => ModuloArithmetic.sign_extend(k, ts.head)
              case BvOp.IntToBv(n) => ModuloArithmetic.cast2UnsignedBV(n, ts.head)
              case BvOp.Nand | BvOp.Nor | BvOp.Xnor | BvOp.UbvToInt | BvOp.SbvToInt | BvOp.Repeat(_) |
                  BvOp.RotateLeft(_) | BvOp.RotateRight(_) =>
                throw UnsupportedOperationException(s"${bop.name} is not encoded for princess")
          case Op.Select(dst, arraySort, array, index) =>
            slots(dst) = IFunApp(arrays(arraySort).select, Seq(term(array), term(index)))
          case Op.Store(dst, arraySort, array, index, value) =>
            slots(dst) = IFunApp(arrays(arraySort).store, Seq(term(array), term(index), term(value)))
          case Op.Ite(dst, c, t, e) =>
            slots(dst) = slots(t) match
              case _: IFormula => IFormulaITE(formula(c), formula(t), formula(e))
              case _ => ITermITE(formula(c), term(t), term(e))
          case Op.True(dst) => slots(dst) = i(true)
          case Op.False(dst) => slots(dst) = i(false)
          case Op.Not(dst, a) => slots(dst) = !formula(a)
          case Op.And(dst, as) =>
            operands(dst) = ("and", as)
            slots(dst) = and(flatten("and", as).map(formula))
          case Op.Or(dst, as) =>
            operands(dst) = ("or", as)
            slots(dst) = or(flatten("or", as).map(formula))
          case Op.Implies(dst, a, b) => slots(dst) = formula(a) ==> formula(b)
          case Op.Xor(dst, a, b) => slots(dst) = formula(a) </> formula(b)
          case Op.Iff(dst, a, b) => slots(dst) = formula(a) <=> formula(b)
          case Op.Eq(dst, as) => slots(dst) = pairs(as)(_ === _)
          case Op.Distinct(dst, as) => slots(dst) = distinct(terms(as))
          case Op.Cmp(dst, cop, real, as) =>
            slots(dst) = cop match
              case CmpOp.Le => if real then pairs(as)(Rationals.leq) else pairs(as)(_ <= _)
              case CmpOp.Lt => if real then pairs(as)(Rationals.lt) else pairs(as)(_ < _)
              case CmpOp.Ge => if real then pairs(as)(Rationals.geq) else pairs(as)(_ >= _)
              case CmpOp.Gt => if real then pairs(as)(Rationals.gt) else pairs(as)(_ > _)
          case Op.BvCmp(dst, cop, l, r) =>
            slots(dst) = cop match
              case BvCmpOp.Ult => ModuloArithmetic.bvult(term(l), term(r))
              case BvCmpOp.Ule => ModuloArithmetic.bvule(term(l), term(r))
              case BvCmpOp.Ugt => ModuloArithmetic.bvugt(term(l), term(r))
              case BvCmpOp.Uge => ModuloArithmetic.bvuge(term(l), term(r))
              case BvCmpOp.Slt => ModuloArithmetic.bvslt(term(l), term(r))
              case BvCmpOp.Sle => ModuloArithmetic.bvsle(term(l), term(r))
              case BvCmpOp.Sgt => ModuloArithmetic.bvsgt(term(l), term(r))
              case BvCmpOp.Sge => ModuloArithmetic.bvsge(term(l), term(r))
          case Op.IsCtor(dst, ctor, arg) =>
            val (adt, k) = ctors(ctor)
            slots(dst) = adt.hasCtor(term(arg), k)
          case Op.IsInt(dst, a) => slots(dst) = Rationals.isInt(term(a))
          case Op.Assert(slot) => p.addAssertion(formula(slot))
          case Op.Push(levels) => for _ <- 1 to levels do p.push()
          case Op.Pop(levels) => for _ <- 1 to levels do p.pop()
          case Op.Check =>
            answers += (p.checkSat(true) match
              case SimpleAPI.ProverStatus.Sat | SimpleAPI.ProverStatus.Valid => true
              case SimpleAPI.ProverStatus.Unsat | SimpleAPI.ProverStatus.Invalid => false
              case status => throw Replay.Undecided("princess", status.toString))
      answers.toList
    }
