package io.bovel.lia

class RationalTest extends munit.FunSuite:
  test("normalization"):
    assertEquals(Rational(2, 4), Rational(1, 2))
    assertEquals(Rational(3, -6), Rational(-1, 2))
    assertEquals(Rational(0, -5), Rational.zero)
    assertEquals(Rational(6, 3).toString, "2")
    assertEquals(Rational(-1, 3).toString, "-1/3")

  test("arithmetic and order"):
    assertEquals(Rational(1, 2) + Rational(1, 3), Rational(5, 6))
    assertEquals(Rational(1, 2) - Rational(1, 3), Rational(1, 6))
    assertEquals(Rational(2, 3) * Rational(3, 4), Rational(1, 2))
    assertEquals(Rational(2, 3) / Rational(4, 3), Rational(1, 2))
    assertEquals(-Rational(1, 2), Rational(-1, 2))
    assert(Rational(1, 3) < Rational(1, 2))
    assert(Rational(-1, 2) < Rational(-1, 3))
    assert(Rational(2).isInteger && !Rational(1, 2).isInteger)

  test("floor and ceil"):
    assertEquals(Rational(7, 2).floor, BigInt(3))
    assertEquals(Rational(7, 2).ceil, BigInt(4))
    assertEquals(Rational(-7, 2).floor, BigInt(-4))
    assertEquals(Rational(-7, 2).ceil, BigInt(-3))
    assertEquals(Rational(-4).floor, BigInt(-4))
    assertEquals(Rational(-4).ceil, BigInt(-4))

  test("arithmetic and order near the bounds of Long"):
    val rng = new scala.util.Random(117)
    val edges = List(
      0L,
      1L,
      2L,
      3L,
      (1L << 31) - 1,
      1L << 31,
      (1L << 32) + 1,
      3037000499L,
      3037000500L,
      (1L << 62) - 1,
      1L << 62,
      Long.MaxValue - 1,
      Long.MaxValue,
      Long.MinValue + 1,
      Long.MinValue
    )
    def pick(): BigInt =
      val v = rng.nextInt(4) match
        case 0 => BigInt(edges(rng.nextInt(edges.length)))
        case 1 => BigInt(rng.nextLong())
        case 2 => BigInt(rng.nextLong() >> rng.nextInt(64))
        case _ => BigInt(rng.nextInt(7) - 3)
      if rng.nextBoolean() then -v else v
    def pickNonZero(): BigInt =
      val v = pick(); if v == 0 then BigInt(1) else v
    def exact(r: Rational): (BigInt, BigInt) = (r.num, r.den)
    // The reference: the arithmetic of fractions in `BigInt`, normalized.
    def norm(n: BigInt, d: BigInt): (BigInt, BigInt) =
      val g = n.gcd(d) * d.signum
      (n / g, d / g)
    for _ <- 1 to 20000 do
      val (n1, d1, n2, d2) = (pick(), pickNonZero(), pick(), pickNonZero())
      val (x, y) = (Rational(n1, d1), Rational(n2, d2))
      val (a1, b1) = norm(n1, d1)
      val (a2, b2) = norm(n2, d2)
      assertEquals(exact(x + y), norm(a1 * b2 + a2 * b1, b1 * b2), s"$x + $y")
      assertEquals(exact(x - y), norm(a1 * b2 - a2 * b1, b1 * b2), s"$x - $y")
      assertEquals(exact(x * y), norm(a1 * a2, b1 * b2), s"$x * $y")
      if a2 != 0 then assertEquals(exact(x / y), norm(a1 * b2, b1 * a2), s"$x / $y")
      assertEquals(x.compare(y).sign, (a1 * b2).compare(a2 * b1).sign, s"$x compare $y")
