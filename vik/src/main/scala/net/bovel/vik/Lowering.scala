package net.bovel.vik

import scala.collection.mutable.{ArrayBuffer, HashMap}

import net.bovel.smt2 as s2
import Formula.*
import Term.*

/** Lowers a surface script (`net.bovel.smt2`) to the solver's language: the one
 *  function from the language the parser reads to the trees the solver plays —
 *  [[Player.run]] applies it, and `smt2lowered` prints its result. Bit-vectors
 *  go to linear integer arithmetic: an `n`-bit term is an `Int` term in
 *  `[0, 2^n)` — a bit-vector declaration becomes an `Int` declaration with its
 *  range asserted, and each operation a linear relation between its arguments,
 *  its result and fresh bounded integers, declared and asserted before the
 *  assertion using it:
 *    - `bvadd`, `bvsub`, `bvneg`, `bvmul` and `bvshl` by a constant, `bvnot`:
 *      the exact value is `result + 2^n × k` for an overflow count `k` in a
 *      known range (none for `bvnot`, `2^n − 1 − x`);
 *    - `bvudiv`, `bvurem`, `bvlshr` by a constant, `extract`, `bvand` with a
 *      mask of low or high bits: `x = d × q + r` with `0 ≤ r < d`;
 *    - `concat` and `zero_extend` are linear; `sign_extend` and the signed
 *      comparisons split off the sign bit, `x = u + 2^(n−1) × s`.
 *  Multiplication, division and shifts by a variable, and the other bitwise
 *  operations would need bit-blasting: they are rejected
 *  ([[net.bovel.smt2.Unsupported]]).
 */
object Lowering:
  def apply(script: s2.Script): Script = Script(Lowerer().commands(script.commands))

/** One lowering: the declared sorts (for the widths of constants), the fresh
 *  constants of the assertion being lowered and their defining assertions.
 */
private final class Lowerer:
  private val sorts = HashMap.empty[String, String] // declared name -> sort (a function's whole arrow sort)
  private var fresh = 0
  private val introduced = ArrayBuffer.empty[String] // fresh Int constants, declared before the assertion
  private val definitions = ArrayBuffer.empty[Formula] // the assertions defining them
  private var depth = 0 // enclosing lambda parameters: no fresh constants under a binder

  /** The operations already lowered for the current assertion: `(= a b)` on
   *  integers is two bounds over the same subtrees, which must share their
   *  fresh constants, not mint them twice. Sound within one assertion — an
   *  operation's constants are determined by its value, and the definitions
   *  flush with the assertion — and cleared with it: after a `pop` the
   *  definitions are gone, so nothing may remember the constants.
   */
  private val memo = HashMap.empty[s2.Term, Term]

  def commands(cs: List[s2.Command]): List[Command] = cs.flatMap(command)

  private def command(c: s2.Command): List[Command] = c match
    // The lowered script is in the corresponding integer logic.
    case s2.Command.SetLogic(logic) => List(Command.SetLogic(logic.replace("BV", "LIA")))
    case s2.Command.SetInfo(key, value) => List(Command.SetInfo(key, value))
    case s2.Command.DeclareSort(name, arity) => List(Command.DeclareSort(name, arity))
    case s2.Command.DeclareFun(name, Nil, sort) if s2.BitVectors.width(sort) >= 0 =>
      sorts(name) = sort
      List(
        Command.DeclareFun(name, Nil, "Int"),
        Command.Assert(range(Const(name), s2.BitVectors.width(sort)))
      )
    case s2.Command.DeclareFun(name, argSorts, sort) =>
      sorts(name) = if argSorts.isEmpty then sort else s2.Sorts.function(argSorts, sort)
      List(Command.DeclareFun(name, argSorts, sort))
    case s2.Command.DeclareDatatypes(datatypes) =>
      for d <- datatypes; ctor <- d.constructors; (selector, sort) <- ctor.selectors do
        sorts(selector) = s2.Sorts.function(List(d.name), sort)
      List(Command.DeclareDatatypes(datatypes))
    case s2.Command.DefineFun(name, params, sort, body) => List(Command.DefineFun(name, params, sort, body))
    case s2.Command.Assert(f) => flush(Command.Assert(formula(f)))
    case s2.Command.AssertNamed(name, f) => flush(Command.AssertNamed(name, formula(f)))
    case s2.Command.CheckSat => List(Command.CheckSat)
    case s2.Command.CheckSatAssuming(assumptions) => flush(Command.CheckSatAssuming(assumptions.map(formula)))
    case s2.Command.Push(levels) => List(Command.Push(levels))
    case s2.Command.Pop(levels) => List(Command.Pop(levels))
    case s2.Command.ResetAssertions => List(Command.ResetAssertions)
    case s2.Command.Reset => List(Command.Reset)
    case s2.Command.GetModel => List(Command.GetModel)
    case s2.Command.GetValue(items) =>
      // The value of a compound operation would need its defining assertions, which cannot be
      // asserted after a check: an item is lowered without them (constants and literals need none).
      val lowered = items.map((source, item) =>
        (
          source,
          (item match
            case f: s2.Formula => formula(f)
            case t: s2.Term => term(t)
          ): Formula | Term
        )
      )
      introduced.clear()
      definitions.clear()
      memo.clear()
      List(Command.GetValue(lowered))
    case s2.Command.GetUnsatCore => List(Command.GetUnsatCore)
    case s2.Command.GetAssertions => List(Command.GetAssertions)
    case s2.Command.Exit => List(Command.Exit)
    case s2.Command.SetOption(key, value) => List(Command.SetOption(key, value))
    case s2.Command.GetInfo(key) => List(Command.GetInfo(key))
    case s2.Command.Other(sexpr) => List(Command.Other(sexpr))

  /** The introduced declarations and defining assertions, then the command. */
  private def flush(command: Command): List[Command] =
    val out = introduced.toList.map(Command.DeclareFun(_, Nil, "Int")) ++
      definitions.toList.map(Command.Assert(_)) :+ command
    introduced.clear()
    definitions.clear()
    memo.clear()
    out

  private def formula(f: s2.Formula): Formula = f match
    case s2.Formula.True => True
    case s2.Formula.False => False
    case s2.Formula.Not(arg) => Not(formula(arg))
    case s2.Formula.And(args) => And(args.map(formula))
    case s2.Formula.Or(args) => Or(args.map(formula))
    case s2.Formula.Implies(lhs, rhs) => Implies(formula(lhs), formula(rhs))
    case s2.Formula.Iff(lhs, rhs) => Iff(formula(lhs), formula(rhs))
    case s2.Formula.Ite(cond, thn, els) => Ite(formula(cond), formula(thn), formula(els))
    case s2.Formula.Eq(lhs, rhs) => Eq(term(lhs), term(rhs))
    // `pred`, not `Pred`: a lowered comparison comes back as a Boolean term and unwraps.
    case s2.Formula.Pred(t) => Formula.pred(term(t))
    case s2.Formula.Le(lhs, rhs) => Le(term(lhs), term(rhs))
    case s2.Formula.Less(lhs, rhs) => Less(term(lhs), term(rhs))

  /** The width of an unlowered bit-vector term, -1 for another sort. */
  private def width(t: s2.Term): Int = t match
    case s2.Term.Bits(_, w) => w
    case s2.Term.Bv("extract", i :: j :: Nil, _) => i - j + 1
    case s2.Term.Bv("zero_extend" | "sign_extend", k :: Nil, a :: Nil) => width(a) + k
    case s2.Term.Bv("concat", _, args) => args.map(width).sum
    case s2.Term.Bv(_, _, args) => width(args.head) // the arithmetic operations keep their arguments' width
    case s2.Term.Const(name) => sorts.get(name).map(s2.BitVectors.width).getOrElse(-1)
    case s2.Term.App(s2.Term.Const(name), args) =>
      sorts.get(name).map(s => s2.BitVectors.width(s2.Sorts.applied(s, args.length))).getOrElse(-1)
    case _ => -1

  /** The term, lowered — a structural copy into the solver's language, raw
   *  constructors throughout: the surface tree is already in the canonical form
   *  the parser built (`Term.app` would flatten a `store!` application it
   *  deliberately kept whole).
   */
  private def term(t: s2.Term): Term = t match
    case s2.Term.Const(name) => Const(name)
    case s2.Term.Num(value) => Num(value)
    case s2.Term.Bits(value, _) => Num(value)
    case bv: s2.Term.Bv => memo.getOrElseUpdate(bv, operation(bv))
    case s2.Term.App(f, as) => App(term(f), as.map(term))
    case s2.Term.Linear(terms, constant) => Linear(terms.map((a, u) => (a, term(u))), constant)
    case s2.Term.Lambda(paramSorts, body) =>
      depth += paramSorts.length
      try Lambda(paramSorts, term(body))
      finally depth -= paramSorts.length
    case s2.Term.Var(index) => Var(index)
    case s2.Term.Bool(f) => Term.boolean(formula(f))

  private def operation(bv: s2.Term.Bv): Term = bv match
    case s2.Term.Bv(op, Nil, a :: b :: Nil) if s2.BitVectors.Comparison.names(op) =>
      Term.boolean(compare(op, term(a), term(b), width(a)))
    case s2.Term.Bv("extract", i :: j :: Nil, a :: Nil) => extract(i, j, term(a), width(a))
    case s2.Term.Bv("zero_extend", _, a :: Nil) => term(a)
    case s2.Term.Bv("sign_extend", k :: Nil, a :: Nil) => signExtend(k, term(a), width(a))
    case s2.Term.Bv("concat", _, args) =>
      args.map(a => (term(a), width(a))).reduceLeft { case ((hi, wh), (lo, wl)) =>
        (Term.linear(List((BigInt(2).pow(wl), hi), (BigInt(1), lo))), wh + wl)
      }._1
    case s2.Term.Bv(op, _, args) => arithmetic(op, args.map(term), width(args.head))

  /** A fresh `Int` constant in `[lo, hi]`, declared and bounded before the
   *  current assertion.
   */
  private def freshInt(prefix: String, lo: BigInt, hi: BigInt): Term =
    if depth > 0 then throw s2.Unsupported("bit-vector operations under a lambda are not supported")
    fresh += 1
    val name = s"$prefix!$fresh"
    introduced += name
    val k = Const(name)
    definitions += Ge(k, Num(lo))
    definitions += Le(k, Num(hi))
    k

  /** `0 ≤ t < 2^width`. */
  private def range(t: Term, width: Int): Formula = And(List(Ge(t, Num(0)), Le(t, Num(modulus(width) - 1))))

  private def modulus(width: Int): BigInt = BigInt(2).pow(width)

  /** `value` reduced modulo `2^width`: a fresh result `r` with
   *  `value = r + 2^width × k`, the overflow count `k` in `[lo, hi]`.
   */
  private def modulo(value: Term, width: Int, lo: BigInt, hi: BigInt): Term = value match
    case Num(v) => Num(v.mod(modulus(width)))
    case _ =>
      val r = freshInt("bv", 0, modulus(width) - 1)
      val k = freshInt("k", lo, hi)
      definitions += IntEq(value, r + k * modulus(width))
      r

  /** `value = divisor × q + r` with `0 ≤ r < divisor` and `q ≤ quotientMax`:
   *  the pair `(q, r)`.
   */
  private def divide(value: Term, divisor: BigInt, quotientMax: BigInt): (Term, Term) = value match
    case Num(v) => (Num(v / divisor), Num(v.mod(divisor)))
    case _ =>
      val q = freshInt("q", 0, quotientMax)
      val r = freshInt("r", 0, divisor - 1)
      definitions += IntEq(value, q * divisor + r)
      (q, r)

  /** The signed value of an `width`-bit term: `t − 2^width × s` for its sign
   *  bit `s`, `t = u + 2^(width−1) × s`.
   */
  private def signed(t: Term, width: Int): Term = t match
    case Num(v) => Num(if v >= modulus(width - 1) then v - modulus(width) else v)
    case _ =>
      val s = freshInt("s", 0, 1)
      val u = freshInt("u", 0, modulus(width - 1) - 1)
      definitions += IntEq(t, u + s * modulus(width - 1))
      t - s * modulus(width)

  private def arithmetic(op: String, args: List[Term], width: Int): Term =
    val m = modulus(width)
    (op, args) match
      case ("bvadd", a :: b :: Nil) => modulo(a + b, width, 0, 1)
      case ("bvsub", a :: b :: Nil) => modulo(a - b, width, -1, 0)
      case ("bvneg", a :: Nil) => modulo(Term.linear(List((BigInt(-1), a))), width, -1, 0)
      case ("bvnot", a :: Nil) => Term.linear(List((BigInt(-1), a)), m - 1)
      case ("bvmul", a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num((u * v).mod(m))
          case (t, Num(c)) => times(t, c.mod(m), width)
          case (Num(c), t) => times(t, c.mod(m), width)
          case _ => throw s2.Unsupported("bvmul of two variables is not supported (no bit-blasting)")
      case ("bvshl", a :: Num(c) :: Nil) =>
        if c >= width then Num(0) else times(a, BigInt(2).pow(c.toInt), width)
      case ("bvlshr", a :: Num(c) :: Nil) =>
        if c >= width then Num(0) else divide(a, BigInt(2).pow(c.toInt), m / BigInt(2).pow(c.toInt) - 1)._1
      case ("bvudiv", a :: Num(c) :: Nil) => if c.signum == 0 then Num(m - 1) else divide(a, c, (m - 1) / c)._1
      case ("bvurem", a :: Num(c) :: Nil) => if c.signum == 0 then a else divide(a, c, (m - 1) / c)._2
      case ("bvand", a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num(u & v)
          case (t, Num(mask)) => masked(t, mask, width)
          case (Num(mask), t) => masked(t, mask, width)
          case _ => throw s2.Unsupported("bvand of two variables is not supported (no bit-blasting)")
      case _ => throw s2.Unsupported(s"$op with these arguments is not supported (no bit-blasting)")
    end match

  /** `c × t` modulo `2^width`, for `0 ≤ c < 2^width`. */
  private def times(t: Term, c: BigInt, width: Int): Term =
    if c.signum == 0 then Num(0) else modulo(t * c, width, 0, c - 1)

  /** `t` masked by `mask`: the low bits (`mask = 2^k − 1`) or the high bits
   *  (`mask = 2^width − 2^k`).
   */
  private def masked(t: Term, mask: BigInt, width: Int): Term =
    val m = modulus(width)
    val low = (0 to width).find(k => mask == BigInt(2).pow(k) - 1)
    val high = (0 to width).find(k => mask == m - BigInt(2).pow(k))
    (low, high) match
      case (Some(k), _) => if k == width then t else divide(t, BigInt(2).pow(k), m / BigInt(2).pow(k) - 1)._2
      case (_, Some(k)) => if k == 0 then t else t - divide(t, BigInt(2).pow(k), m / BigInt(2).pow(k) - 1)._2
      case _ => throw s2.Unsupported(s"bvand with the mask $mask is not supported (no bit-blasting)")

  /** Bits `i` down to `j` of `t`: `t = 2^j × q + low`,
   *  `q = 2^(i−j+1) × high + bits`.
   */
  private def extract(i: Int, j: Int, t: Term, width: Int): Term =
    val q = if j == 0 then t else divide(t, BigInt(2).pow(j), modulus(width - j) - 1)._1
    if i == width - 1 then q else divide(q, modulus(i - j + 1), modulus(width - i - 1) - 1)._2

  /** `t` extended by `k` copies of its sign bit:
   *  `t + s × (2^(width+k) − 2^width)`.
   */
  private def signExtend(k: Int, t: Term, width: Int): Term =
    if k == 0 then t
    else Term.linear(List((BigInt(1), t), (modulus(width + k) - modulus(width), signBit(t, width))))

  private def signBit(t: Term, width: Int): Term = t match
    case Num(v) => Num(if v >= modulus(width - 1) then 1 else 0)
    case _ =>
      val s = freshInt("s", 0, 1)
      val u = freshInt("u", 0, modulus(width - 1) - 1)
      definitions += IntEq(t, u + s * modulus(width - 1))
      s

  private def compare(op: String, a: Term, b: Term, width: Int): Formula =
    val (l, r) = if op.startsWith("bvs") then (signed(a, width), signed(b, width)) else (a, b)
    op.drop(3) match
      case "lt" => Lt(l, r)
      case "le" => Le(l, r)
      case "gt" => Gt(l, r)
      case _ => Ge(l, r)
