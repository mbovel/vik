package net.bovel.smt2

import Formula.*
import Term.*

/** The lowering of the arithmetic fragment of bit-vectors to integers, for the
 *  the front end: an `n`-bit term is an `Int` term in `[0, 2^n)`, and each
 *  operation is a linear relation between its arguments, its result and fresh
 *  bounded integers, asserted before the assertion using it:
 *    - `bvadd`, `bvsub`, `bvneg`, `bvmul` and `bvshl` by a constant, `bvnot`:
 *      the exact value is `result + 2^n × k` for an overflow count `k` in a
 *      known range (none for `bvnot`, `2^n − 1 − x`);
 *    - `bvudiv`, `bvurem`, `bvlshr` by a constant, `extract`, `bvand` with a
 *      mask of low or high bits: `x = d × q + r` with `0 ≤ r < d`;
 *    - `concat` and `zero_extend` are linear; `sign_extend` and the signed
 *      comparisons split off the sign bit, `x = u + 2^(n−1) × s`.
 *  Multiplication, division and shifts by a variable, and the other bitwise
 *  operations would need bit-blasting: they are rejected.
 */
object BitVectors:
  private val Sort = """\(_ BitVec (\d+)\)""".r

  /** The width of a bit-vector sort, -1 for another sort. */
  def width(sort: String): Int = sort match
    case Sort(n) => n.nn.toInt
    case _ => -1

  def sort(width: Int): String = s"(_ BitVec $width)"

  /** `0 ≤ t < 2^width`. */
  def range(t: Term, width: Int): Formula = And(List(Ge(t, Num(0)), Le(t, Num(modulus(width) - 1))))

  private def modulus(width: Int): BigInt = BigInt(2).pow(width)

  object Arithmetic:
    val names: Set[String] =
      Set("bvadd", "bvsub", "bvneg", "bvmul", "bvnot", "bvshl", "bvlshr", "bvudiv", "bvurem", "bvand")
    def unapply(name: String): Option[String] = Option.when(names(name))(name)

  object Comparison:
    val names: Set[String] = Set("bvult", "bvule", "bvugt", "bvuge", "bvslt", "bvsle", "bvsgt", "bvsge")
    def unapply(name: String): Option[String] = Option.when(names(name))(name)

  /** `value` reduced modulo `2^width`: a fresh result `r` with
   *  `value = r + 2^width × k`, the overflow count `k` in `[lo, hi]`.
   */
  private def modulo(value: Term, width: Int, lo: BigInt, hi: BigInt, target: Lowering): Term = value match
    case Num(v) => Num(v.mod(modulus(width)))
    case _ =>
      val r = target.freshInt("bv", 0, modulus(width) - 1)
      val k = target.freshInt("k", lo, hi)
      target.define(IntEq(value, r + k * modulus(width)))
      r

  /** `value = divisor × q + r` with `0 ≤ r < divisor` and `q ≤ quotientMax`:
   *  the pair `(q, r)`.
   */
  private def divide(value: Term, divisor: BigInt, quotientMax: BigInt, target: Lowering): (Term, Term) = value match
    case Num(v) => (Num(v / divisor), Num(v.mod(divisor)))
    case _ =>
      val q = target.freshInt("q", 0, quotientMax)
      val r = target.freshInt("r", 0, divisor - 1)
      target.define(IntEq(value, q * divisor + r))
      (q, r)

  /** The signed value of an `width`-bit term: `t − 2^width × s` for its sign
   *  bit `s`, `t = u + 2^(width−1) × s`.
   */
  private def signed(t: Term, width: Int, target: Lowering): Term = t match
    case Num(v) => Num(if v >= modulus(width - 1) then v - modulus(width) else v)
    case _ =>
      val s = target.freshInt("s", 0, 1)
      val u = target.freshInt("u", 0, modulus(width - 1) - 1)
      target.define(IntEq(t, u + s * modulus(width - 1)))
      t - s * modulus(width)

  def arithmetic(op: String, args: List[Term], width: Int, target: Lowering): Term =
    val m = modulus(width)
    (op, args) match
      case ("bvadd", a :: b :: Nil) => modulo(a + b, width, 0, 1, target)
      case ("bvsub", a :: b :: Nil) => modulo(a - b, width, -1, 0, target)
      case ("bvneg", a :: Nil) => modulo(Term.linear(List((BigInt(-1), a))), width, -1, 0, target)
      case ("bvnot", a :: Nil) => Term.linear(List((BigInt(-1), a)), m - 1)
      case ("bvmul", a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num((u * v).mod(m))
          case (t, Num(c)) => times(t, c.mod(m), width, target)
          case (Num(c), t) => times(t, c.mod(m), width, target)
          case _ => throw Unsupported("bvmul of two variables is not supported (no bit-blasting)")
      case ("bvshl", a :: Num(c) :: Nil) =>
        if c >= width then Num(0) else times(a, BigInt(2).pow(c.toInt), width, target)
      case ("bvlshr", a :: Num(c) :: Nil) =>
        if c >= width then Num(0) else divide(a, BigInt(2).pow(c.toInt), m / BigInt(2).pow(c.toInt) - 1, target)._1
      case ("bvudiv", a :: Num(c) :: Nil) => if c.signum == 0 then Num(m - 1) else divide(a, c, (m - 1) / c, target)._1
      case ("bvurem", a :: Num(c) :: Nil) => if c.signum == 0 then a else divide(a, c, (m - 1) / c, target)._2
      case ("bvand", a :: b :: Nil) =>
        (a, b) match
          case (Num(u), Num(v)) => Num(u & v)
          case (t, Num(mask)) => masked(t, mask, width, target)
          case (Num(mask), t) => masked(t, mask, width, target)
          case _ => throw Unsupported("bvand of two variables is not supported (no bit-blasting)")
      case _ => throw Unsupported(s"$op with these arguments is not supported (no bit-blasting)")
    end match

  /** `c × t` modulo `2^width`, for `0 ≤ c < 2^width`. */
  private def times(t: Term, c: BigInt, width: Int, target: Lowering): Term =
    if c.signum == 0 then Num(0) else modulo(t * c, width, 0, c - 1, target)

  /** `t` masked by `mask`: the low bits (`mask = 2^k − 1`) or the high bits
   *  (`mask = 2^width − 2^k`).
   */
  private def masked(t: Term, mask: BigInt, width: Int, target: Lowering): Term =
    val m = modulus(width)
    val low = (0 to width).find(k => mask == BigInt(2).pow(k) - 1)
    val high = (0 to width).find(k => mask == m - BigInt(2).pow(k))
    (low, high) match
      case (Some(k), _) => if k == width then t else divide(t, BigInt(2).pow(k), m / BigInt(2).pow(k) - 1, target)._2
      case (_, Some(k)) => if k == 0 then t else t - divide(t, BigInt(2).pow(k), m / BigInt(2).pow(k) - 1, target)._2
      case _ => throw Unsupported(s"bvand with the mask $mask is not supported (no bit-blasting)")

  /** Bits `i` down to `j` of `t`: `t = 2^j × q + low`,
   *  `q = 2^(i−j+1) × high + bits`.
   */
  def extract(i: Int, j: Int, t: Term, width: Int, target: Lowering): Term =
    val q = if j == 0 then t else divide(t, BigInt(2).pow(j), modulus(width - j) - 1, target)._1
    if i == width - 1 then q else divide(q, modulus(i - j + 1), modulus(width - i - 1) - 1, target)._2

  /** `t` extended by `k` copies of its sign bit:
   *  `t + s × (2^(width+k) − 2^width)`.
   */
  def signExtend(k: Int, t: Term, width: Int, target: Lowering): Term =
    if k == 0 then t
    else Term.linear(List((BigInt(1), t), (modulus(width + k) - modulus(width), signBit(t, width, target))))

  private def signBit(t: Term, width: Int, target: Lowering): Term = t match
    case Num(v) => Num(if v >= modulus(width - 1) then 1 else 0)
    case _ =>
      val s = target.freshInt("s", 0, 1)
      val u = target.freshInt("u", 0, modulus(width - 1) - 1)
      target.define(IntEq(t, u + s * modulus(width - 1)))
      s

  def compare(op: String, a: Term, b: Term, width: Int, target: Lowering): Formula =
    val (l, r) = if op.startsWith("bvs") then (signed(a, width, target), signed(b, width, target)) else (a, b)
    op.drop(3) match
      case "lt" => Lt(l, r)
      case "le" => Le(l, r)
      case "gt" => Gt(l, r)
      case _ => Ge(l, r)
