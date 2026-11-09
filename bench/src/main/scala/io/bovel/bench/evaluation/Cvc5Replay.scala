package io.bovel.bench.evaluation

import scala.collection.mutable.ArrayBuffer

import io.github.cvc5.{Kind, Solver, Sort, Term, TermManager}
import io.bovel.smtlib.{ArithOp, BvCmpOp, BvOp, CmpOp}
import io.bovel.smtlib.Sort as SmtSort

/** Runs a [[Replay]] through cvc5's Java API: what a verifier that builds
 *  cvc5's terms directly executes per query. The program is replayed on a fresh
 *  term manager and solver, so it pays declarations, term construction, the
 *  checks and the models, nothing else; a script that pushes gets the
 *  `incremental` option and one that asks for a model `produce-models`, as its
 *  text does through the parser.
 *
 *  The binding keeps every term, sort and solver it creates until
 *  `Context.deletePointers()` frees them all at once — the caller's call, after
 *  the run: it would pull the objects from under a run still going elsewhere.
 */
object Cvc5Replay:
  import Replay.{Op, decided}

  /** Replays the program against a fresh solver — asserts, `push`, `pop`,
   *  `check-sat` and `get-model` in script order — and returns the answers.
   */
  def check(program: Replay): List[Boolean] =
    val tm = TermManager()
    val solver = Solver(tm)
    try
      if program.logic != null then solver.setLogic(program.logic)
      if program.ops.exists(_.isInstanceOf[Op.Push]) || program.resets then solver.setOption("incremental", "true")
      if program.models then solver.setOption("produce-models", "true")
      // What a model reports: the API, unlike `get-model`, is told. The binding's declarations are global.
      val declaredSorts = ArrayBuffer.empty[Sort]
      val declaredFuns = ArrayBuffer.empty[Term]
      val slots = new Array[Term](program.slotCount)
      val decls = new Array[Term](program.declCount)
      val testers = new Array[Term](program.declCount) // by the constructor's id
      val sorts = new Array[Sort](program.sortCount)
      val answers = ArrayBuffer.empty[Boolean]
      def args(is: Array[Int]): Array[Term] = is.map(slots)
      def mk(kind: Kind, is: Array[Int]): Term = tm.mkTerm(kind, args(is))
      def apply(kind: Kind, fun: Term, is: Array[Int]): Term = tm.mkTerm(kind, fun +: args(is))
      def indexed(kind: Kind, indices: Int*)(is: Int*): Term =
        tm.mkTerm(tm.mkOp(kind, indices.toArray), is.map(slots).toArray)
      for op <- program.ops do
        op match
          case Op.MkSort(id, sort) =>
            sorts(id) = sort match
              case SmtSort.Bool => tm.getBooleanSort
              case SmtSort.Int => tm.getIntegerSort
              case SmtSort.Real => tm.getRealSort
              case SmtSort.BitVec(width) => tm.mkBitVectorSort(width)
              case _ => throw IllegalStateException(s"not a built-in sort: $sort")
          case Op.ArraySort(id, index, element) => sorts(id) = tm.mkArraySort(sorts(index), sorts(element))
          case Op.DeclareSort(id, name) =>
            sorts(id) = tm.mkUninterpretedSort(name)
            declaredSorts += sorts(id)
          case Op.DeclareDatatypes(datatypes, sortIds, ctorIds, selectorIds) =>
            // A field sort naming a datatype of the group is an unresolved sort, resolved with the group.
            val unresolved = datatypes.map(d => d.name -> tm.mkUnresolvedDatatypeSort(d.name)).toMap
            val decls0 = datatypes.map { d =>
              val decl = tm.mkDatatypeDecl(d.name)
              for c <- d.constructors do
                val ctor = tm.mkDatatypeConstructorDecl(c.name)
                for (selector, s) <- c.selectors do
                  ctor.addSelector(selector, unresolved.getOrElse(s.text, sorts(sortIds(s))))
                decl.addConstructor(ctor)
              decl
            }
            val created = tm.mkDatatypeSorts(decls0.toArray)
            for (d, i) <- datatypes.zipWithIndex do
              sorts(sortIds(SmtSort.Named(d.name))) = created(i)
              val datatype = created(i).getDatatype
              for c <- d.constructors do
                val constructor = datatype.getConstructor(c.name)
                decls(ctorIds(c.name)) = constructor.getTerm
                testers(ctorIds(c.name)) = constructor.getTesterTerm
                for (selector, _) <- c.selectors do
                  decls(selectorIds(selector)) = constructor.getSelector(selector).getTerm
          case Op.DeclareFun(id, name, argSorts, result) =>
            decls(id) =
              if argSorts.isEmpty then tm.mkConst(sorts(result), name)
              else tm.mkConst(tm.mkFunctionSort(argSorts.map(sorts), sorts(result)), name)
            declaredFuns += decls(id)
          case Op.Const(dst, decl) =>
            val fun = decls(decl)
            slots(dst) = if fun.getSort.isDatatypeConstructor then tm.mkTerm(Kind.APPLY_CONSTRUCTOR, fun) else fun
          case Op.App(dst, decl, as) =>
            val fun = decls(decl)
            slots(dst) =
              if fun.getSort.isDatatypeConstructor then apply(Kind.APPLY_CONSTRUCTOR, fun, as)
              else if fun.getSort.isDatatypeSelector then apply(Kind.APPLY_SELECTOR, fun, as)
              else apply(Kind.APPLY_UF, fun, as)
          case Op.Numeral(dst, value, real) =>
            slots(dst) = if real then tm.mkReal(value.toString) else tm.mkInteger(value.toString)
          case Op.Decimal(dst, unscaled, scale) => slots(dst) = tm.mkReal(s"$unscaled/1${"0" * scale}")
          case Op.Bits(dst, value, width) => slots(dst) = tm.mkBitVector(width, value.toString, 10)
          case Op.Arith(dst, aop, _, as) =>
            slots(dst) = aop match
              case ArithOp.Add => mk(Kind.ADD, as)
              case ArithOp.Sub => mk(Kind.SUB, as)
              case ArithOp.Neg => mk(Kind.NEG, as)
              case ArithOp.Mul => mk(Kind.MULT, as)
              case ArithOp.Div => mk(Kind.DIVISION, as)
              case ArithOp.IntDiv => mk(Kind.INTS_DIVISION, as)
              case ArithOp.Mod => mk(Kind.INTS_MODULUS, as)
              case ArithOp.Abs => mk(Kind.ABS, as)
              case ArithOp.ToReal => mk(Kind.TO_REAL, as)
              case ArithOp.ToInt => mk(Kind.TO_INTEGER, as)
          case Op.Bv(dst, bop, as) =>
            slots(dst) = bop match
              case BvOp.Add => mk(Kind.BITVECTOR_ADD, as)
              case BvOp.Sub => mk(Kind.BITVECTOR_SUB, as)
              case BvOp.Neg => mk(Kind.BITVECTOR_NEG, as)
              case BvOp.Not => mk(Kind.BITVECTOR_NOT, as)
              case BvOp.Mul => mk(Kind.BITVECTOR_MULT, as)
              case BvOp.And => mk(Kind.BITVECTOR_AND, as)
              case BvOp.Or => mk(Kind.BITVECTOR_OR, as)
              case BvOp.Nand => mk(Kind.BITVECTOR_NAND, as)
              case BvOp.Nor => mk(Kind.BITVECTOR_NOR, as)
              case BvOp.Xor => mk(Kind.BITVECTOR_XOR, as)
              case BvOp.Xnor => mk(Kind.BITVECTOR_XNOR, as)
              case BvOp.Comp => mk(Kind.BITVECTOR_COMP, as)
              case BvOp.Shl => mk(Kind.BITVECTOR_SHL, as)
              case BvOp.Lshr => mk(Kind.BITVECTOR_LSHR, as)
              case BvOp.Ashr => mk(Kind.BITVECTOR_ASHR, as)
              case BvOp.Udiv => mk(Kind.BITVECTOR_UDIV, as)
              case BvOp.Sdiv => mk(Kind.BITVECTOR_SDIV, as)
              case BvOp.Urem => mk(Kind.BITVECTOR_UREM, as)
              case BvOp.Srem => mk(Kind.BITVECTOR_SREM, as)
              case BvOp.Smod => mk(Kind.BITVECTOR_SMOD, as)
              case BvOp.Concat => mk(Kind.BITVECTOR_CONCAT, as)
              case BvOp.UbvToInt => mk(Kind.BITVECTOR_UBV_TO_INT, as)
              case BvOp.SbvToInt => mk(Kind.BITVECTOR_SBV_TO_INT, as)
              case BvOp.Extract(i, j) => indexed(Kind.BITVECTOR_EXTRACT, i, j)(as(0))
              case BvOp.ZeroExtend(k) => indexed(Kind.BITVECTOR_ZERO_EXTEND, k)(as(0))
              case BvOp.SignExtend(k) => indexed(Kind.BITVECTOR_SIGN_EXTEND, k)(as(0))
              case BvOp.IntToBv(n) => indexed(Kind.INT_TO_BITVECTOR, n)(as(0))
              case BvOp.Repeat(k) => indexed(Kind.BITVECTOR_REPEAT, k)(as(0))
              case BvOp.RotateLeft(k) => indexed(Kind.BITVECTOR_ROTATE_LEFT, k)(as(0))
              case BvOp.RotateRight(k) => indexed(Kind.BITVECTOR_ROTATE_RIGHT, k)(as(0))
          case Op.Select(dst, _, array, index) => slots(dst) = tm.mkTerm(Kind.SELECT, slots(array), slots(index))
          case Op.Store(dst, _, array, index, value) =>
            slots(dst) = tm.mkTerm(Kind.STORE, slots(array), slots(index), slots(value))
          case Op.Ite(dst, c, t, e) => slots(dst) = tm.mkTerm(Kind.ITE, slots(c), slots(t), slots(e))
          case Op.True(dst) => slots(dst) = tm.mkTrue()
          case Op.False(dst) => slots(dst) = tm.mkFalse()
          case Op.Not(dst, a) => slots(dst) = tm.mkTerm(Kind.NOT, slots(a))
          case Op.And(dst, as) => slots(dst) = mk(Kind.AND, as)
          case Op.Or(dst, as) => slots(dst) = mk(Kind.OR, as)
          case Op.Implies(dst, a, b) => slots(dst) = tm.mkTerm(Kind.IMPLIES, slots(a), slots(b))
          case Op.Xor(dst, a, b) => slots(dst) = tm.mkTerm(Kind.XOR, slots(a), slots(b))
          case Op.Iff(dst, a, b) => slots(dst) = tm.mkTerm(Kind.EQUAL, slots(a), slots(b))
          case Op.Eq(dst, as) => slots(dst) = mk(Kind.EQUAL, as) // chainable: cvc5 builds the chain
          case Op.Distinct(dst, as) => slots(dst) = mk(Kind.DISTINCT, as)
          case Op.Cmp(dst, cop, _, as) =>
            slots(dst) = cop match
              case CmpOp.Le => mk(Kind.LEQ, as)
              case CmpOp.Lt => mk(Kind.LT, as)
              case CmpOp.Ge => mk(Kind.GEQ, as)
              case CmpOp.Gt => mk(Kind.GT, as)
          case Op.BvCmp(dst, cop, l, r) =>
            val kind = cop match
              case BvCmpOp.Ult => Kind.BITVECTOR_ULT
              case BvCmpOp.Ule => Kind.BITVECTOR_ULE
              case BvCmpOp.Ugt => Kind.BITVECTOR_UGT
              case BvCmpOp.Uge => Kind.BITVECTOR_UGE
              case BvCmpOp.Slt => Kind.BITVECTOR_SLT
              case BvCmpOp.Sle => Kind.BITVECTOR_SLE
              case BvCmpOp.Sgt => Kind.BITVECTOR_SGT
              case BvCmpOp.Sge => Kind.BITVECTOR_SGE
            slots(dst) = tm.mkTerm(kind, slots(l), slots(r))
          case Op.IsCtor(dst, ctor, arg) => slots(dst) = tm.mkTerm(Kind.APPLY_TESTER, testers(ctor), slots(arg))
          case Op.IsInt(dst, a) => slots(dst) = tm.mkTerm(Kind.IS_INTEGER, slots(a))
          case Op.Assert(slot) => solver.assertFormula(slots(slot))
          case Op.Push(levels) => solver.push(levels)
          case Op.Pop(levels) => solver.pop(levels)
          case Op.SetOption(_, _) => () // for VIK's session; the binding's declarations are global
          case Op.SetLogic(_) => () // given once, the first, at the start
          case Op.Reset => solver.resetAssertions() // the terms declared through the API stay valid
          case Op.ResetAll =>
            solver.resetAssertions()
            declaredSorts.clear()
            declaredFuns.clear()
          case Op.Check =>
            val result = solver.checkSat()
            answers += decided(if result.isSat then Some(true) else if result.isUnsat then Some(false) else None, "cvc5")
          case Op.GetModel => if answers.last then solver.getModel(declaredSorts.toArray, declaredFuns.toArray)
      answers.toList
    finally solver.deletePointer()
