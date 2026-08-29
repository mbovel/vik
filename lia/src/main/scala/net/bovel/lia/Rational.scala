package net.bovel.lia

/** An arbitrary-precision rational number, normalized: the denominator is
 *  positive and coprime with the numerator.
 */
final class Rational private (val num: BigInt, val den: BigInt) extends Ordered[Rational]:
  def +(that: Rational): Rational = Rational(num * that.den + that.num * den, den * that.den)
  def -(that: Rational): Rational = Rational(num * that.den - that.num * den, den * that.den)
  def *(that: Rational): Rational = Rational(num * that.num, den * that.den)
  def /(that: Rational): Rational =
    require(that.num.signum != 0, "division by zero")
    Rational(num * that.den, den * that.num)
  def unary_- : Rational = new Rational(-num, den)

  def compare(that: Rational): Int = (num * that.den).compare(that.num * den)
  def signum: Int = num.signum
  def isZero: Boolean = num.signum == 0
  def isInteger: Boolean = den == 1

  /** The greatest integer not above this number. */
  def floor: BigInt = (num - num.mod(den)) / den

  /** The least integer not below this number. */
  def ceil: BigInt = -(-this).floor

  override def equals(that: Any): Boolean = that match
    case that: Rational => num == that.num && den == that.den
    case _ => false
  override def hashCode: Int = 31 * num.hashCode + den.hashCode
  override def toString: String = if den == 1 then num.toString else s"$num/$den"

object Rational:
  val zero: Rational = new Rational(0, 1)
  val one: Rational = new Rational(1, 1)

  def apply(num: BigInt, den: BigInt): Rational =
    require(den.signum != 0, "zero denominator")
    val g = num.gcd(den) * den.signum
    if g == 1 then new Rational(num, den) else new Rational(num / g, den / g)

  def apply(n: BigInt): Rational = new Rational(n, 1)
  def apply(n: Int): Rational = new Rational(n, 1)
