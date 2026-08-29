package net.bovel.lia

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
