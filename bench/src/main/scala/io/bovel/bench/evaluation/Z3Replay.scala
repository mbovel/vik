package io.bovel.bench.evaluation

import scala.collection.mutable.ArrayBuffer

import com.microsoft.z3.{
  ArithSort,
  ArraySort,
  BitVecExpr,
  BitVecSort,
  BoolExpr,
  Context,
  Expr,
  FuncDecl,
  IntExpr,
  RealExpr,
  Sort,
  Status
}
import io.bovel.smtlib.{ArithOp, BvCmpOp, BvOp, CmpOp}
import io.bovel.smtlib.Sort as SmtSort

/** Runs a [[Replay]] through z3's Java API: what a verifier that builds z3's
 *  terms directly executes per query. The program is replayed in the trial's
 *  context against a fresh solver, so it pays declarations, term construction
 *  and the checks, nothing else.
 */
object Z3Replay:
  import Replay.Op

  /** A fresh solver: z3's for `logic` when the script names one (what the
   *  binary runs on the file), the default one otherwise.
   */
  def solver(ctx: Context, logic: String | Null): com.microsoft.z3.Solver =
    if logic == null then ctx.mkSolver() else ctx.mkSolver(logic)

  /** Replays the program in `ctx` against a fresh solver — asserts, `push`,
   *  `pop` and `check-sat` in script order — and returns the answers. The
   *  solver is reset on the way out: the binding frees a solver's native side
   *  only once the GC has collected its wrapper (`Z3ReferenceQueue`; there is
   *  no release call), and a program that allocates little on the Java side
   *  collects late — the finished solvers pile up in the context, and each new
   *  one slows with their number (2 ms to 77 ms per replay of the incremental
   *  `QF_BV` instance over 2000 replays in one context; flat at 1.3 ms reset).
   */
  def check(program: Replay, ctx: Context): List[Boolean] =
    val solver = Z3Replay.solver(ctx, program.logic)
    try replay(program, ctx, solver)
    finally solver.reset()

  private def replay(program: Replay, ctx: Context, solver: com.microsoft.z3.Solver): List[Boolean] =
    val slots = new Array[Expr[?]](program.slotCount)
    val decls = new Array[FuncDecl[?]](program.declCount)
    val testers = new Array[FuncDecl[?]](program.declCount) // by the constructor's id
    val sorts = new Array[Sort](program.sortCount)
    val answers = ArrayBuffer.empty[Boolean]
    def bool(i: Int): BoolExpr = slots(i).asInstanceOf[BoolExpr]
    def arith(i: Int): Expr[ArithSort] = slots(i).asInstanceOf[Expr[ArithSort]]
    def bv(i: Int): BitVecExpr = slots(i).asInstanceOf[BitVecExpr]
    def args(is: Array[Int]): Array[Expr[?]] = is.map(slots)
    def pairs(is: Array[Int])(f: (Int, Int) => BoolExpr): BoolExpr = // a chainable operator
      if is.length == 2 then f(is(0), is(1))
      else ctx.mkAnd(is.zip(is.tail).map(f(_, _))*)
    for op <- program.ops do
      op match
        case Op.MkSort(id, sort) =>
          sorts(id) = sort match
            case SmtSort.Bool => ctx.getBoolSort
            case SmtSort.Int => ctx.getIntSort
            case SmtSort.Real => ctx.getRealSort
            case SmtSort.BitVec(width) => ctx.mkBitVecSort(width)
            case _ => throw IllegalStateException(s"not a built-in sort: $sort")
        case Op.ArraySort(id, index, element) => sorts(id) = ctx.mkArraySort(sorts(index), sorts(element))
        case Op.DeclareSort(id, name) => sorts(id) = ctx.mkUninterpretedSort(name)
        case Op.DeclareDatatypes(datatypes, sortIds, ctorIds, selectorIds) =>
          // Mutually recursive datatypes: a field sort naming a datatype of the group is a reference by index.
          val names = datatypes.map(_.name)
          val constructors = datatypes.map { d =>
            d.constructors.map { c =>
              val fieldSorts = c.selectors.map((_, s) => sortIds.get(s).map(sorts).orNull)
              val refs = c.selectors.map((_, s) => names.indexOf(s.text))
              ctx.mkConstructor(c.name, c.tester, c.selectors.map(_._1).toArray, fieldSorts.toArray, refs.toArray)
            }.toArray
          }
          val created = ctx.mkDatatypeSorts(names.toArray, constructors.toArray)
          for (d, i) <- datatypes.zipWithIndex do
            sorts(sortIds(SmtSort.Named(d.name))) = created(i)
            for (c, k) <- d.constructors.zipWithIndex do
              val constructor = constructors(i)(k)
              decls(ctorIds(c.name)) = constructor.ConstructorDecl()
              testers(ctorIds(c.name)) = constructor.getTesterDecl
              for ((selector, _), j) <- c.selectors.zipWithIndex do
                decls(selectorIds(selector)) = constructor.getAccessorDecls()(j)
        case Op.DeclareFun(id, name, argSorts, result) =>
          decls(id) = ctx.mkFuncDecl(name, argSorts.map(sorts), sorts(result))
        case Op.Const(dst, decl) => slots(dst) = ctx.mkConst(decls(decl))
        case Op.App(dst, decl, as) => slots(dst) = ctx.mkApp(decls(decl), args(as)*)
        case Op.Numeral(dst, value, real) =>
          slots(dst) = if real then ctx.mkReal(value.toString) else ctx.mkInt(value.toString)
        case Op.Decimal(dst, unscaled, scale) => slots(dst) = ctx.mkReal(s"$unscaled/1${"0" * scale}")
        case Op.Bits(dst, value, width) => slots(dst) = ctx.mkBV(value.toString, width)
        case Op.Arith(dst, aop, _, as) =>
          slots(dst) = aop match
            case ArithOp.Add => ctx.mkAdd(as.map(arith)*)
            case ArithOp.Sub => ctx.mkSub(as.map(arith)*)
            case ArithOp.Neg => ctx.mkUnaryMinus(arith(as(0)))
            case ArithOp.Mul => ctx.mkMul(as.map(arith)*)
            case ArithOp.Div | ArithOp.IntDiv => // n-ary, left-associative (issue 53)
              as.map(arith).reduceLeft[Expr[ArithSort]]((a, b) => ctx.mkDiv(a, b))
            case ArithOp.Mod => ctx.mkMod(slots(as(0)).asInstanceOf[IntExpr], slots(as(1)).asInstanceOf[IntExpr])
            case ArithOp.Abs => throw UnsupportedOperationException("abs is not in z3's Java API")
            case ArithOp.ToReal => ctx.mkInt2Real(slots(as(0)).asInstanceOf[IntExpr])
            case ArithOp.ToInt => ctx.mkReal2Int(slots(as(0)).asInstanceOf[RealExpr])
        case Op.Bv(dst, bop, as) =>
          def fold(f: (BitVecExpr, BitVecExpr) => BitVecExpr): BitVecExpr = as.map(bv).reduceLeft(f)
          slots(dst) = bop match
            case BvOp.Add => fold(ctx.mkBVAdd)
            case BvOp.Sub => fold(ctx.mkBVSub)
            case BvOp.Neg => ctx.mkBVNeg(bv(as(0)))
            case BvOp.Not => ctx.mkBVNot(bv(as(0)))
            case BvOp.Mul => fold(ctx.mkBVMul)
            case BvOp.And => fold(ctx.mkBVAND)
            case BvOp.Or => fold(ctx.mkBVOR)
            case BvOp.Nand => ctx.mkBVNAND(bv(as(0)), bv(as(1)))
            case BvOp.Nor => ctx.mkBVNOR(bv(as(0)), bv(as(1)))
            case BvOp.Xor => fold(ctx.mkBVXOR)
            case BvOp.Xnor => ctx.mkBVXNOR(bv(as(0)), bv(as(1)))
            case BvOp.Comp => throw UnsupportedOperationException("bvcomp is not in z3's Java API")
            case BvOp.Shl => ctx.mkBVSHL(bv(as(0)), bv(as(1)))
            case BvOp.Lshr => ctx.mkBVLSHR(bv(as(0)), bv(as(1)))
            case BvOp.Ashr => ctx.mkBVASHR(bv(as(0)), bv(as(1)))
            case BvOp.Udiv => ctx.mkBVUDiv(bv(as(0)), bv(as(1)))
            case BvOp.Sdiv => ctx.mkBVSDiv(bv(as(0)), bv(as(1)))
            case BvOp.Urem => ctx.mkBVURem(bv(as(0)), bv(as(1)))
            case BvOp.Srem => ctx.mkBVSRem(bv(as(0)), bv(as(1)))
            case BvOp.Smod => ctx.mkBVSMod(bv(as(0)), bv(as(1)))
            case BvOp.Concat => fold(ctx.mkConcat)
            case BvOp.UbvToInt => ctx.mkBV2Int(bv(as(0)), false)
            case BvOp.SbvToInt => ctx.mkBV2Int(bv(as(0)), true)
            case BvOp.Extract(i, j) => ctx.mkExtract(i, j, bv(as(0)))
            case BvOp.ZeroExtend(k) => ctx.mkZeroExt(k, bv(as(0)))
            case BvOp.SignExtend(k) => ctx.mkSignExt(k, bv(as(0)))
            case BvOp.IntToBv(n) => ctx.mkInt2BV(n, slots(as(0)).asInstanceOf[IntExpr])
            case BvOp.Repeat(k) => ctx.mkRepeat(k, bv(as(0)))
            case BvOp.RotateLeft(k) => ctx.mkBVRotateLeft(k, bv(as(0)))
            case BvOp.RotateRight(k) => ctx.mkBVRotateRight(k, bv(as(0)))
        case Op.Select(dst, _, array, index) =>
          slots(dst) =
            ctx.mkSelect(slots(array).asInstanceOf[Expr[ArraySort[Sort, Sort]]], slots(index).asInstanceOf[Expr[Sort]])
        case Op.Store(dst, _, array, index, value) =>
          slots(dst) = ctx.mkStore(
            slots(array).asInstanceOf[Expr[ArraySort[Sort, Sort]]],
            slots(index).asInstanceOf[Expr[Sort]],
            slots(value).asInstanceOf[Expr[Sort]]
          )
        case Op.Ite(dst, c, t, e) => slots(dst) = ctx.mkITE(bool(c), slots(t), slots(e))
        case Op.True(dst) => slots(dst) = ctx.mkTrue()
        case Op.False(dst) => slots(dst) = ctx.mkFalse()
        case Op.Not(dst, a) => slots(dst) = ctx.mkNot(bool(a))
        case Op.And(dst, as) => slots(dst) = ctx.mkAnd(as.map(bool)*)
        case Op.Or(dst, as) => slots(dst) = ctx.mkOr(as.map(bool)*)
        case Op.Implies(dst, a, b) => slots(dst) = ctx.mkImplies(bool(a), bool(b))
        case Op.Xor(dst, a, b) => slots(dst) = ctx.mkXor(bool(a), bool(b))
        case Op.Iff(dst, a, b) => slots(dst) = ctx.mkIff(bool(a), bool(b))
        case Op.Eq(dst, as) => slots(dst) = pairs(as)((l, r) => ctx.mkEq(slots(l), slots(r)))
        case Op.Distinct(dst, as) => slots(dst) = ctx.mkDistinct(args(as)*)
        case Op.Cmp(dst, cop, _, as) =>
          slots(dst) = cop match
            case CmpOp.Le => pairs(as)((l, r) => ctx.mkLe(arith(l), arith(r)))
            case CmpOp.Lt => pairs(as)((l, r) => ctx.mkLt(arith(l), arith(r)))
            case CmpOp.Ge => pairs(as)((l, r) => ctx.mkGe(arith(l), arith(r)))
            case CmpOp.Gt => pairs(as)((l, r) => ctx.mkGt(arith(l), arith(r)))
        case Op.BvCmp(dst, cop, l, r) =>
          slots(dst) = cop match
            case BvCmpOp.Ult => ctx.mkBVULT(bv(l), bv(r))
            case BvCmpOp.Ule => ctx.mkBVULE(bv(l), bv(r))
            case BvCmpOp.Ugt => ctx.mkBVUGT(bv(l), bv(r))
            case BvCmpOp.Uge => ctx.mkBVUGE(bv(l), bv(r))
            case BvCmpOp.Slt => ctx.mkBVSLT(bv(l), bv(r))
            case BvCmpOp.Sle => ctx.mkBVSLE(bv(l), bv(r))
            case BvCmpOp.Sgt => ctx.mkBVSGT(bv(l), bv(r))
            case BvCmpOp.Sge => ctx.mkBVSGE(bv(l), bv(r))
        case Op.IsCtor(dst, ctor, arg) => slots(dst) = ctx.mkApp(testers(ctor), slots(arg))
        case Op.IsInt(dst, a) => slots(dst) = ctx.mkIsInteger(slots(a).asInstanceOf[RealExpr])
        case Op.Assert(slot) => solver.add(bool(slot))
        case Op.Push(levels) => for _ <- 1 to levels do solver.push()
        case Op.Pop(levels) => solver.pop(levels)
        case Op.SetOption(_, _) => () // for VIK's session; the binding's declarations are global
        case Op.SetLogic(_) => () // given once, the first, at the start
        case Op.Reset => solver.reset() // the assertions and the levels; the context keeps the declarations
        case Op.ResetAll => solver.reset() // the declarations are made again at new ids; the old ones are harmless
        case Op.Check =>
          // `VIK_REPLAY_DUMP`: the solver's assertions as SMT-LIB before each check, to compare the
          // replayed problem with the script when their answers differ.
          if sys.env.contains("VIK_REPLAY_DUMP") then System.err.println(s"; check\n${solver.toString}")
          answers += (solver.check() match
            case Status.SATISFIABLE => true
            case Status.UNSATISFIABLE => false
            case status => throw Replay.Undecided("z3", status.toString))
    answers.toList
