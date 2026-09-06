package io.bovel.lia

/** An arbitrary-precision rational number, normalized: the denominator is
 *  positive and coprime with the numerator. Numbers whose numerator and
 *  denominator fit in a `Long` are computed with `Long` arithmetic (with
 *  overflow detection, falling back to `BigInt`), which is what the simplex
 *  handles almost all the time.
 */
sealed abstract class Rational extends Ordered[Rational]:
  def num: BigInt
  def den: BigInt

  def +(that: Rational): Rational = (this, that) match
    case (Rational.Small(n1, d1), Rational.Small(n2, d2)) =>
      try
        Rational.small(
          Math.addExact(Math.multiplyExact(n1, d2), Math.multiplyExact(n2, d1)),
          Math.multiplyExact(d1, d2)
        )
      catch case _: ArithmeticException => Rational(num * that.den + that.num * den, den * that.den)
    case _ => Rational(num * that.den + that.num * den, den * that.den)

  def -(that: Rational): Rational = this + -that

  def *(that: Rational): Rational = (this, that) match
    case (Rational.Small(n1, d1), Rational.Small(n2, d2)) =>
      try Rational.small(Math.multiplyExact(n1, n2), Math.multiplyExact(d1, d2))
      catch case _: ArithmeticException => Rational(num * that.num, den * that.den)
    case _ => Rational(num * that.num, den * that.den)

  def /(that: Rational): Rational =
    require(that.signum != 0, "division by zero")
    (this, that) match
      case (Rational.Small(n1, d1), Rational.Small(n2, d2)) =>
        try Rational.small(Math.multiplyExact(n1, d2), Math.multiplyExact(d1, n2))
        catch case _: ArithmeticException => Rational(num * that.den, den * that.num)
      case _ => Rational(num * that.den, den * that.num)

  def unary_- : Rational

  def compare(that: Rational): Int = (this, that) match
    case (Rational.Small(n1, d1), Rational.Small(n2, d2)) =>
      try java.lang.Long.compare(Math.multiplyExact(n1, d2), Math.multiplyExact(n2, d1))
      catch case _: ArithmeticException => (num * that.den).compare(that.num * den)
    case _ => (num * that.den).compare(that.num * den)

  def signum: Int
  def isZero: Boolean = signum == 0
  def isInteger: Boolean

  /** The greatest integer not above this number. */
  def floor: BigInt = (num - num.mod(den)) / den

  /** The least integer not below this number. */
  def ceil: BigInt = -(-this).floor

  override def equals(that: Any): Boolean = that match
    case that: Rational => compare(that) == 0
    case _ => false
  override def hashCode: Int = 31 * Rational.hash(num) + Rational.hash(den)
  override def toString: String = if isInteger then num.toString else s"$num/$den"

object Rational:
  /** A big integer's hash, the same on every platform: `BigInt` hashes a value
   *  past the `Long` range through `BigInteger.hashCode`, which the JDK and the
   *  javalib of Scala.js and Scala Native compute differently (issue 58), and a
   *  hash map over terms then iterates in another order — the model, the
   *  numbering of the lowering's constants differ. This is the JDK's:
   *  `31 * h + word` over the 32-bit words of the magnitude, most significant
   *  first, times the sign; on the JVM the very value `BigInt.hashCode` gives.
   */
  def hash(v: BigInt): Int =
    if v.isValidLong then v.hashCode
    else
      val magnitude = v.abs
      val words = (magnitude.bitLength + 31) / 32
      var h = 0
      var i = words - 1
      while i >= 0 do
        h = 31 * h + ((magnitude >> (32 * i)) & 0xffffffffL).toInt
        i -= 1
      h * v.signum

  private final case class Small(n: Long, d: Long) extends Rational:
    def num: BigInt = BigInt(n)
    def den: BigInt = BigInt(d)
    def unary_- : Rational = if n == Long.MinValue then Rational(-num, den) else Small(-n, d)
    def signum: Int = java.lang.Long.signum(n)
    def isInteger: Boolean = d == 1
    override def floor: BigInt = BigInt(Math.floorDiv(n, d))

  private final case class Big(num: BigInt, den: BigInt) extends Rational:
    def unary_- : Rational = Big(-num, den)
    def signum: Int = num.signum
    def isInteger: Boolean = den == 1

  val zero: Rational = Small(0, 1)
  val one: Rational = Small(1, 1)

  def apply(num: BigInt, den: BigInt): Rational =
    require(den.signum != 0, "zero denominator")
    val g = num.gcd(den) * den.signum
    val (n, d) = if g == 1 then (num, den) else (num / g, den / g)
    if n.isValidLong && d.isValidLong then Small(n.toLong, d.toLong) else Big(n, d)

  def apply(n: BigInt): Rational = if n.isValidLong then Small(n.toLong, 1) else Big(n, 1)
  def apply(n: Int): Rational = Small(n, 1)

  /** `n / d` normalized, for `d ≠ 0`, both far from overflow. */
  private def small(n: Long, d: Long): Rational =
    if n == Long.MinValue || d == Long.MinValue then Rational(BigInt(n), BigInt(d))
    else if d == 1 then Small(n, 1) // integers, the common case: no gcd to take
    else
      val g = gcd(Math.abs(n), Math.abs(d)) * java.lang.Long.signum(d)
      Small(n / g, d / g)

  private def gcd(a: Long, b: Long): Long = if b == 0 then a else gcd(b, a % b)
