package net.bovel.bench

import scala.collection.mutable.{ArrayBuffer, HashMap}

import net.bovel.smtlib.{ArithOp, BvCmpOp, BvOp, CmpOp, Command, Datatype, Formula, Script, Sort, Term}

/** An instance compiled to a straight-line program of SMT-LIB's own operations
 *  over numbered slots: what a verifier that builds a solver's terms directly
 *  executes per query, before any solver's API is named. [[Z3Replay]],
 *  [[PrincessReplay]] and [[SmtInterpolReplay]] run one against their solver —
 *  declarations, term construction, asserts, `push`/`pop` and every
 *  `check-sat`, nothing else: no VIK tree is walked, no text is parsed.
 *
 *  [[Replay.compile]] reads the surface script once, before measuring, and does
 *  what every solver's front end does with the text: a `let` binding becomes a
 *  slot its name refers to, a `define-fun` is expanded at each use, a numeral
 *  takes the sort of its position (`Real` beside a real, `to_real` around an
 *  integer term there), and every distinct construction is built once — the way
 *  a builder's cache would. The tree is the script's own: n-ary `=` and `<`,
 *  `distinct`, `ite`, bit-vectors and arrays reach the solver as written, never
 *  VIK's lowering of them.
 *
 *  `named` has the slots the script reaches through a name — a `let` binding's
 *  value, a `define-fun` application — which a parser translates once and then
 *  treats as a unit.
 */
final class Replay(
    val ops: Array[Replay.Op],
    val slotCount: Int,
    val sortCount: Int,
    val declCount: Int,
    val logic: String | Null,
    val named: Set[Int]
)

object Replay:
  enum Op:
    /** A built-in sort: `Bool`, `Int`, `Real` or `(_ BitVec w)`. */
    case MkSort(id: Int, sort: Sort)
    case ArraySort(id: Int, index: Int, element: Int)
    case DeclareSort(id: Int, name: String)

    /** A group of datatypes; `sortIds` has the ids of the group's sorts and of
     *  every field sort, `ctorIds` and `selectorIds` the declaration ids of
     *  their members.
     */
    case DeclareDatatypes(
        datatypes: List[Datatype],
        sortIds: Map[Sort, Int],
        ctorIds: Map[String, Int],
        selectorIds: Map[String, Int]
    )
    case DeclareFun(id: Int, name: String, argSorts: Array[Int], result: Int)
    case Const(dst: Int, decl: Int)
    case App(dst: Int, decl: Int, args: Array[Int])
    case Numeral(dst: Int, value: BigInt, real: Boolean)
    case Decimal(dst: Int, unscaled: BigInt, scale: Int)
    case Bits(dst: Int, value: BigInt, width: Int)

    /** `real`: the operation is the reals' (its arguments are real). */
    case Arith(dst: Int, op: ArithOp, real: Boolean, args: Array[Int])
    case Bv(dst: Int, op: BvOp, args: Array[Int])
    case Select(dst: Int, arraySort: Int, array: Int, index: Int)
    case Store(dst: Int, arraySort: Int, array: Int, index: Int, value: Int)
    case Ite(dst: Int, cond: Int, thn: Int, els: Int)
    case True(dst: Int)
    case False(dst: Int)
    case Not(dst: Int, a: Int)
    case And(dst: Int, args: Array[Int])
    case Or(dst: Int, args: Array[Int])
    case Implies(dst: Int, a: Int, b: Int)
    case Xor(dst: Int, a: Int, b: Int)
    case Iff(dst: Int, a: Int, b: Int)
    case Eq(dst: Int, args: Array[Int])
    case Distinct(dst: Int, args: Array[Int])
    case Cmp(dst: Int, op: CmpOp, real: Boolean, args: Array[Int])
    case BvCmp(dst: Int, op: BvCmpOp, lhs: Int, rhs: Int)
    case IsCtor(dst: Int, ctor: Int, arg: Int)
    case IsInt(dst: Int, a: Int)
    case Assert(slot: Int)
    case Push(levels: Int)
    case Pop(levels: Int)
    case Check

  /** Compiles the script: the only walk of the tree, done before measuring.
   *  Throws on what no replay encodes (lambdas, sort parameters).
   */
  def compile(script: Script): Replay =
    val out = ArrayBuffer.empty[Op]
    val sortIds = HashMap.empty[Sort, Int]
    val declIds = HashMap.empty[String, Int]
    val declSorts = HashMap.empty[String, (List[Sort], Sort)]
    val macros = HashMap.empty[String, (List[(String, Sort)], Term | Formula)] // define-fun
    val slotSorts = ArrayBuffer.empty[Sort]
    val memo = HashMap.empty[Any, Int] // a construction, by its shape over slots, to its slot
    val named = scala.collection.mutable.HashSet.empty[Int]
    var logic: String | Null = null
    // With no integers in the logic a bare numeral is a real (QF_LRA's `(< x 1)`), as the standard
    // reads it; otherwise it is an integer unless its position says real.
    def numeralsReal: Boolean = logic != null && logic.nn.contains('R') && !logic.nn.contains('I')
    type Env = Map[String, Int] // the let- and define-fun-bound names in scope, to their slots

    def emit(key: Any, sort: Sort)(op: Int => Op): Int = memo.getOrElseUpdate(
      key, {
        val dst = slotSorts.length
        slotSorts += sort
        out += op(dst)
        dst
      }
    )
    def sortId(s: Sort): Int = sortIds.get(s) match
      case Some(id) => id
      case None =>
        val op: Int => Op = s match
          case Sort.Bool | Sort.Int | Sort.Real | Sort.BitVec(_) => Op.MkSort(_, s)
          case Sort.Array(index, element) =>
            val (i, e) = (sortId(index), sortId(element))
            Op.ArraySort(_, i, e)
          case Sort.Named(name) => throw IllegalArgumentException(s"undeclared sort $name")
          case Sort.Fun(_, _) => throw UnsupportedOperationException("function sorts are not encoded")
        val id = sortIds.size
        sortIds(s) = id
        out += op(id)
        id
    def width(s: Sort): Int = s match
      case Sort.BitVec(w) => w
      case other => throw IllegalArgumentException(s"a bit-vector was expected, not $other")
    def numeral(value: BigInt, real: Boolean): Int =
      emit(("num", value, real), if real then Sort.Real else Sort.Int)(Op.Numeral(_, value, real))
    def isNumeral(t: Term): Boolean = t.isInstanceOf[Term.Numeral]

    /** `t` where sort `expected` is wanted: an integer term there is wrapped in
     *  `to_real`.
     */
    def termOf(t: Term, env: Env, expected: Sort): Int =
      val slot = term(t, env, expected)
      if expected == Sort.Real && slotSorts(slot) == Sort.Int then
        emit(("to_real", slot), Sort.Real)(Op.Arith(_, ArithOp.ToReal, true, Array(slot)))
      else slot

    /** Sibling terms of one sort (the arguments of `+`, `=`, `<`, the branches
     *  of `ite`): the sort is `expected`, else `Real` if any non-literal
     *  argument is, else the first argument's, else the numerals' default.
     *  Numerals are built for that sort; integer terms are lifted to `Real`.
     */
    def typed(args: List[Term], env: Env, expected: Sort | Null): (Array[Int], Sort) =
      val known = args.map(a => if isNumeral(a) then -1 else term(a, env, expected))
      val sorts = known.filter(_ >= 0).map(slotSorts)
      val sort: Sort =
        if expected != null then expected
        else if sorts.contains(Sort.Real) then Sort.Real
        else sorts.headOption.getOrElse(if numeralsReal then Sort.Real else Sort.Int)
      val slots = args.zip(known).map {
        case (Term.Numeral(v), _) => numeral(v, sort == Sort.Real)
        case (_, slot) =>
          if sort == Sort.Real && slotSorts(slot) == Sort.Int then
            emit(("to_real", slot), Sort.Real)(Op.Arith(_, ArithOp.ToReal, true, Array(slot)))
          else slot
      }
      (slots.toArray, sort)

    def bind(bindings: List[(String, Term | Formula)], env: Env): Env =
      bindings.map { (name, value) =>
        val slot = expr(value, env, null)
        named += slot
        name -> slot
      }.toMap

    def expr(e: Term | Formula, env: Env, expected: Sort | Null): Int = e match
      case t: Term => term(t, env, expected)
      case f: Formula => formula(f, env)

    /** A `define-fun` body, with its parameters bound. */
    def expand(body: Term | Formula, env: Env, expected: Sort | Null): Int =
      val slot = expr(body, env, expected)
      named += slot
      slot

    def term(t: Term, env: Env, expected: Sort | Null): Int = t match
      case Term.Const(name) =>
        env.get(name) match
          case Some(slot) => slot
          case None =>
            macros.get(name) match
              case Some((Nil, body)) => expand(body, Map.empty, expected)
              case _ =>
                val id = declIds.getOrElse(name, throw IllegalArgumentException(s"undeclared $name"))
                emit(("const", id), declSorts(name)._2)(Op.Const(_, id))
      case Term.Numeral(v) => numeral(v, expected == Sort.Real || (expected == null && numeralsReal))
      case Term.Decimal(unscaled, scale) => emit(("dec", unscaled, scale), Sort.Real)(Op.Decimal(_, unscaled, scale))
      case Term.Bits(value, w) => emit(("bits", value, w), Sort.BitVec(w))(Op.Bits(_, value, w))
      case Term.App(Term.Const(name), args) if macros.contains(name) =>
        val (params, body) = macros(name)
        expand(body, params.zip(args).map((p, a) => p._1 -> termOf(a, env, p._2)).toMap, expected)
      case Term.App(Term.Const(name), args) if declSorts.contains(name) =>
        val (argSorts, result) = declSorts(name)
        val id = declIds(name)
        val slots = args.zip(argSorts).map((a, s) => termOf(a, env, s)).toArray
        emit(("app", id, slots.toList), result)(Op.App(_, id, slots))
      case Term.App(fun, _) => throw UnsupportedOperationException(s"higher-order application of $fun")
      case Term.ArithApp(op, args) =>
        op match
          case ArithOp.ToReal =>
            val a = term(args.head, env, Sort.Int)
            if slotSorts(a) == Sort.Real then a
            else emit(("to_real", a), Sort.Real)(Op.Arith(_, op, true, Array(a)))
          case ArithOp.ToInt =>
            val a = termOf(args.head, env, Sort.Real)
            emit(("to_int", a), Sort.Int)(Op.Arith(_, op, true, Array(a)))
          case ArithOp.Div =>
            val (slots, _) = typed(args, env, Sort.Real)
            emit((op, slots.toList), Sort.Real)(Op.Arith(_, op, true, slots))
          case ArithOp.IntDiv | ArithOp.Mod | ArithOp.Abs =>
            val (slots, _) = typed(args, env, Sort.Int)
            emit((op, slots.toList), Sort.Int)(Op.Arith(_, op, false, slots))
          case ArithOp.Add | ArithOp.Sub | ArithOp.Neg | ArithOp.Mul =>
            val (slots, sort) = typed(args, env, expected)
            emit((op, sort, slots.toList), sort)(Op.Arith(_, op, sort == Sort.Real, slots))
      case Term.BvApp(op, args) =>
        val slots = args.map(term(_, env, null)).toArray
        val sorts = slots.map(slotSorts)
        val sort = op match
          case BvOp.Comp => Sort.BitVec(1)
          case BvOp.UbvToInt | BvOp.SbvToInt => Sort.Int
          case BvOp.IntToBv(n) => Sort.BitVec(n)
          case BvOp.Extract(i, j) => Sort.BitVec(i - j + 1)
          case BvOp.ZeroExtend(k) => Sort.BitVec(width(sorts(0)) + k)
          case BvOp.SignExtend(k) => Sort.BitVec(width(sorts(0)) + k)
          case BvOp.Repeat(k) => Sort.BitVec(width(sorts(0)) * k)
          case BvOp.Concat => Sort.BitVec(sorts.map(width).sum)
          case _ => sorts(0)
        emit((op, slots.toList), sort)(Op.Bv(_, op, slots))
      case Term.Select(array, index) =>
        val a = term(array, env, null)
        slotSorts(a) match
          case s @ Sort.Array(indexSort, element) =>
            val i = termOf(index, env, indexSort)
            emit(("select", a, i), element)(Op.Select(_, sortId(s), a, i))
          case other => throw IllegalArgumentException(s"select on $other")
      case Term.Store(array, index, value) =>
        val a = term(array, env, null)
        slotSorts(a) match
          case s @ Sort.Array(indexSort, element) =>
            val i = termOf(index, env, indexSort)
            val v = termOf(value, env, element)
            emit(("store", a, i, v), s)(Op.Store(_, sortId(s), a, i, v))
          case other => throw IllegalArgumentException(s"store on $other")
      case Term.Ite(cond, thn, els) =>
        val c = formula(cond, env)
        val (slots, sort) = typed(List(thn, els), env, expected)
        emit(("ite", c, slots(0), slots(1)), sort)(Op.Ite(_, c, slots(0), slots(1)))
      case Term.Let(bindings, body) => term(body, env ++ bind(bindings, env), expected)
      case Term.Lambda(_, _) => throw UnsupportedOperationException("lambdas are not encoded")
      case Term.Bool(f) => formula(f, env)

    def formula(f: Formula, env: Env): Int = f match
      case Formula.True => emit("true", Sort.Bool)(Op.True(_))
      case Formula.False => emit("false", Sort.Bool)(Op.False(_))
      case Formula.Not(a) =>
        val s = formula(a, env)
        emit(("not", s), Sort.Bool)(Op.Not(_, s))
      case Formula.And(List(a)) => formula(a, env)
      case Formula.And(args) =>
        val slots = args.map(formula(_, env)).toArray
        emit(("and", slots.toList), Sort.Bool)(Op.And(_, slots))
      case Formula.Or(List(a)) => formula(a, env)
      case Formula.Or(args) =>
        val slots = args.map(formula(_, env)).toArray
        emit(("or", slots.toList), Sort.Bool)(Op.Or(_, slots))
      case Formula.Implies(args) => // right-associative
        args.map(formula(_, env)).reduceRight((a, b) => emit(("=>", a, b), Sort.Bool)(Op.Implies(_, a, b)))
      case Formula.Xor(args) => // left-associative
        args.map(formula(_, env)).reduceLeft((a, b) => emit(("xor", a, b), Sort.Bool)(Op.Xor(_, a, b)))
      case Formula.Iff(args) => // chained
        val slots = args.map(formula(_, env))
        val pairs = slots.zip(slots.tail).map((a, b) => emit(("iff", a, b), Sort.Bool)(Op.Iff(_, a, b)))
        if pairs.length == 1 then pairs.head
        else emit(("and", pairs), Sort.Bool)(Op.And(_, pairs.toArray))
      case Formula.Eq(args) =>
        val (slots, _) = typed(args, env, null)
        emit(("=", slots.toList), Sort.Bool)(Op.Eq(_, slots))
      case Formula.Distinct(args) =>
        val (slots, _) = typed(args, env, null)
        emit(("distinct", slots.toList), Sort.Bool)(Op.Distinct(_, slots))
      case Formula.Cmp(op, args) =>
        val (slots, sort) = typed(args, env, null)
        emit((op, slots.toList), Sort.Bool)(Op.Cmp(_, op, sort == Sort.Real, slots))
      case Formula.BvCmp(op, lhs, rhs) =>
        val (l, r) = (term(lhs, env, null), term(rhs, env, null))
        emit((op, l, r), Sort.Bool)(Op.BvCmp(_, op, l, r))
      case Formula.Ite(cond, thn, els) =>
        val (c, t, e) = (formula(cond, env), formula(thn, env), formula(els, env))
        emit(("ite", c, t, e), Sort.Bool)(Op.Ite(_, c, t, e))
      case Formula.IsConstructor(ctor, arg) =>
        val a = term(arg, env, null)
        val id = declIds(ctor)
        emit(("is", id, a), Sort.Bool)(Op.IsCtor(_, id, a))
      case Formula.IsInt(arg) =>
        val a = termOf(arg, env, Sort.Real)
        emit(("is_int", a), Sort.Bool)(Op.IsInt(_, a))
      case Formula.Let(bindings, body) => formula(body, env ++ bind(bindings, env))
      case Formula.Pred(t) => term(t, env, Sort.Bool)

    for command <- script.commands do
      command match
        case Command.SetLogic(l) => logic = l
        case Command.DeclareSort(name, 0) =>
          val id = sortIds.size
          sortIds(Sort.Named(name)) = id
          out += Op.DeclareSort(id, name)
        case Command.DeclareSort(name, _) => throw UnsupportedOperationException(s"sort $name takes parameters")
        case Command.DeclareFun(name, argSorts, sort) =>
          val id = declIds.size
          declIds(name) = id
          declSorts(name) = (argSorts, sort)
          out += Op.DeclareFun(id, name, argSorts.map(sortId).toArray, sortId(sort))
        case Command.DeclareDatatypes(datatypes) =>
          // The datatypes take sort ids silently: the declaration op fills them.
          val group = datatypes.map(d => Sort.Named(d.name))
          for s <- group do sortIds(s) = sortIds.size
          val fieldSorts = datatypes.flatMap(_.constructors.flatMap(_.selectors.map(_._2))).distinct
          val ids = (group ++ fieldSorts).distinct.map(s => s -> sortId(s)).toMap
          val ctorIds = HashMap.empty[String, Int]
          val selectorIds = HashMap.empty[String, Int]
          for d <- datatypes; c <- d.constructors do
            ctorIds(c.name) = declIds.size
            declIds(c.name) = declIds.size
            declSorts(c.name) = (c.selectors.map(_._2), Sort.Named(d.name))
            for (selector, sort) <- c.selectors do
              selectorIds(selector) = declIds.size
              declIds(selector) = declIds.size
              declSorts(selector) = (List(Sort.Named(d.name)), sort)
          out += Op.DeclareDatatypes(datatypes, ids, ctorIds.toMap, selectorIds.toMap)
        case Command.DefineFun(name, params, _, body) => macros(name) = (params, body)
        case Command.Assert(f) => out += Op.Assert(formula(f, Map.empty))
        case Command.AssertNamed(_, f) => out += Op.Assert(formula(f, Map.empty)) // the name changes nothing
        case Command.Push(levels) => out += Op.Push(levels)
        case Command.Pop(levels) => out += Op.Pop(levels)
        case Command.CheckSat => out += Op.Check
        case _ => () // check-sat-assuming, get-*, set-*: not part of what the solvers are measured on
    Replay(out.toArray, slotSorts.length, sortIds.size, declIds.size, logic, named.toSet)
