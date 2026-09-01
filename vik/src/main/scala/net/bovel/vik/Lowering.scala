package net.bovel.vik

import scala.collection.mutable.{ArrayBuffer, HashMap}

import net.bovel.smt2.*
import net.bovel.smt2.Formula.*
import net.bovel.smt2.Term.*

/** Lowers the bit-vectors of a script to linear integer arithmetic — the parser
 *  reads them as they are ([[net.bovel.smt2.Term.Bv]]), and the solver speaks
 *  integers: [[Player.run]] applies this before playing, and `smt2lowered`
 *  prints the result. An `n`-bit term is an `Int` term in `[0, 2^n)`: a
 *  bit-vector declaration becomes an `Int` declaration with its range asserted,
 *  and each operation a linear relation between its arguments, its result and
 *  fresh bounded integers, declared and asserted before the assertion using it:
 *    - `bvadd`, `bvsub`, `bvneg`, `bvmul` and `bvshl` by a constant, `bvnot`:
 *      the exact value is `result + 2^n × k` for an overflow count `k` in a
 *      known range (none for `bvnot`, `2^n − 1 − x`);
 *    - `bvudiv`, `bvurem`, `bvlshr` by a constant, `extract`, `bvand` with a
 *      mask of low or high bits: `x = d × q + r` with `0 ≤ r < d`;
 *    - `concat` and `zero_extend` are linear; `sign_extend` and the signed
 *      comparisons split off the sign bit, `x = u + 2^(n−1) × s`.
 *  Multiplication, division and shifts by a variable, and the other bitwise
 *  operations would need bit-blasting: they are rejected
 *  ([[smt2.Unsupported]]).
 */
object Lowering:
  def apply(script: Script): Script = Script(Lowerer().commands(script.commands))

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
  private val memo = HashMap.empty[Term, Term]

  def commands(cs: List[Command]): List[Command] = cs.flatMap(command)

  private def command(c: Command): List[Command] = c match
    // The lowered script is in the corresponding integer logic.
    case Command.SetLogic(logic) => List(Command.SetLogic(logic.replace("BV", "LIA")))
    case Command.DeclareFun(name, Nil, sort) if BitVectors.width(sort) >= 0 =>
      sorts(name) = sort
      List(Command.DeclareFun(name, Nil, "Int"), Command.Assert(range(Const(name), BitVectors.width(sort))))
    case Command.DeclareFun(name, argSorts, sort) =>
      sorts(name) = if argSorts.isEmpty then sort else Sorts.function(argSorts, sort)
      List(c)
    case Command.DeclareDatatypes(datatypes) =>
      for d <- datatypes; ctor <- d.constructors; (selector, sort) <- ctor.selectors do
        sorts(selector) = Sorts.function(List(d.name), sort)
      List(c)
    case Command.Assert(f) => flush(Command.Assert(formula(f)))
    case Command.AssertNamed(name, f) => flush(Command.AssertNamed(name, formula(f)))
    case Command.CheckSatAssuming(assumptions) => flush(Command.CheckSatAssuming(assumptions.map(formula)))
    case Command.GetValue(items) =>
      // The value of a compound operation would need its defining assertions, which cannot be
      // asserted after a check: an item is lowered without them (constants and literals need none).
      val lowered = items.map((source, item) =>
        (
          source,
          (item match
            case f: Formula => formula(f)
            case t: Term => term(t)
          ): Formula | Term
        )
      )
      introduced.clear()
      definitions.clear()
      memo.clear()
      List(Command.GetValue(lowered))
    case other => List(other)

  /** The introduced declarations and defining assertions, then the command. */
  private def flush(command: Command): List[Command] =
    val out = introduced.toList.map(Command.DeclareFun(_, Nil, "Int")) ++
      definitions.toList.map(Command.Assert(_)) :+ command
    introduced.clear()
    definitions.clear()
    memo.clear()
    out

  private def formula(f: Formula): Formula = f.mapTerms(term)

  /** The width of an unlowered bit-vector term, -1 for another sort. */
  private def width(t: Term): Int = t match
    case Bits(_, w) => w
    case Bv("extract", i :: j :: Nil, _) => i - j + 1
    case Bv("zero_extend" | "sign_extend", k :: Nil, a :: Nil) => width(a) + k
    case Bv("concat", _, args) => args.map(width).sum
    case Bv(_, _, args) => width(args.head) // the arithmetic operations keep their arguments' width
    case Const(name) => sorts.get(name).map(BitVectors.width).getOrElse(-1)
    case App(Const(name), args) =>
      sorts.get(name).map(s => BitVectors.width(Sorts.applied(s, args.length))).getOrElse(-1)
    case _ => -1

  private def term(t: Term): Term = t match
    case Bits(value, _) => Num(value)
    case bv: Bv => memo.getOrElseUpdate(bv, operation(bv))
    case Const(_) | Num(_) | Var(_) => t
    case App(f, as) =>
      val fun = term(f)
      val args = as.map(term)
      // Unchanged applications stay as they are: `Term.app` would re-canonicalize a raw `App`.
      if (fun eq f) && args.corresponds(as)(_ eq _) then t else Term.app(fun, args)
    case Linear(terms, constant) => Term.linear(terms.map((a, u) => (a, term(u))), constant)
    case Lambda(paramSorts, body) =>
      depth += paramSorts.length
      try Lambda(paramSorts, term(body))
      finally depth -= paramSorts.length
    case Bool(f) => Term.boolean(formula(f))

  private def operation(bv: Term.Bv): Term = bv match
    case Bv(op, Nil, a :: b :: Nil) if BitVectors.Comparison.names(op) =>
      Term.boolean(compare(op, term(a), term(b), width(a)))
    case Bv("extract", i :: j :: Nil, a :: Nil) => extract(i, j, term(a), width(a))
    case Bv("zero_extend", _, a :: Nil) => term(a)
    case Bv("sign_extend", k :: Nil, a :: Nil) => signExtend(k, term(a), width(a))
    case Bv("concat", _, args) =>
      args.map(a => (term(a), width(a))).reduceLeft { case ((hi, wh), (lo, wl)) =>
        (Term.linear(List((BigInt(2).pow(wl), hi), (BigInt(1), lo))), wh + wl)
      }._1
    case Bv(op, _, args) => arithmetic(op, args.map(term), width(args.head))

  /** A fresh `Int` constant in `[lo, hi]`, declared and bounded before the
   *  current assertion.
   */
  private def freshInt(prefix: String, lo: BigInt, hi: BigInt): Term =
    if depth > 0 then throw Unsupported("bit-vector operations under a lambda are not supported")
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
          case _ => throw Unsupported("bvmul of two variables is not supported (no bit-blasting)")
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
          case _ => throw Unsupported("bvand of two variables is not supported (no bit-blasting)")
      case _ => throw Unsupported(s"$op with these arguments is not supported (no bit-blasting)")
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
      case _ => throw Unsupported(s"bvand with the mask $mask is not supported (no bit-blasting)")

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
