package net.bovel.lia

/** A rational with an infinitesimal part, `r + d·δ` for a positive δ smaller
 *  than any rational the problem can produce: how a strict bound becomes an
 *  inclusive one (`x < c` is `x ≤ c − δ`), after Dutertre and de Moura. The
 *  order is lexicographic; a concrete δ exists whenever a system of inclusive
 *  δ-bounds is satisfiable, and [[Solver]] resolves one for the model.
 */
final case class Delta(r: Rational, d: Rational) extends Ordered[Delta]:
  // An integer problem never leaves d = 0: every operation short-circuits it, so the pure-integer
  // path costs one rational operation, not two.
  def +(that: Delta): Delta =
    if d.isZero && that.d.isZero then Delta(r + that.r, Rational.zero) else Delta(r + that.r, d + that.d)
  def -(that: Delta): Delta =
    if d.isZero && that.d.isZero then Delta(r - that.r, Rational.zero) else Delta(r - that.r, d - that.d)
  def *(c: Rational): Delta = if d.isZero then Delta(r * c, Rational.zero) else Delta(r * c, d * c)
  def /(c: Rational): Delta = if d.isZero then Delta(r / c, Rational.zero) else Delta(r / c, d / c)
  def unary_- : Delta = if d.isZero then Delta(-r, Rational.zero) else Delta(-r, -d)
  def compare(that: Delta): Int =
    val first = r.compare(that.r)
    if first != 0 then first
    else if d.isZero && that.d.isZero then 0
    else d.compare(that.d)
  def isInteger: Boolean = d.isZero && r.isInteger
  def floor: BigInt = if d.signum < 0 && r.isInteger then r.floor - 1 else r.floor
  def ceil: BigInt = if d.signum > 0 && r.isInteger then r.ceil + 1 else r.ceil
  override def toString: String = if d.isZero then r.toString else s"$r${if d.signum < 0 then "-" else "+"}${d}δ"

object Delta:
  val zero: Delta = Delta(Rational.zero, Rational.zero)
  def apply(r: Rational): Delta = Delta(r, Rational.zero)
  def strictUpper(r: Rational): Delta = Delta(r, Rational(-1))
  def strictLower(r: Rational): Delta = Delta(r, Rational(1))
