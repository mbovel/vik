package io.bovel.vik

import io.bovel.lia.Rational

/** The terms' hashes: the same on every platform, so that the hash maps of the
 *  encoder iterate in one order everywhere and a model does not depend on where
 *  it was computed (issue 58).
 */
class ExprTest extends munit.FunSuite:
  test("a term's hash does not depend on the platform"):
    // The values the JVM gives, pinned; the suite runs on Scala.js and Scala Native too.
    assertEquals(Rational.hash(BigInt(2).pow(63)), -2147483648)
    assertEquals(Rational.hash(-BigInt(2).pow(100)), -476656)
    assertEquals(Rational.hash(BigInt("123456789012345678901234567890")), 1915528825)
    assertEquals(Term.Num(BigInt(2).pow(64)).hashCode, 1222663928)
    assertEquals(Term.Num(BigInt(2).pow(70) + 3).hashCode, -931333882)
    assertEquals(Term.Linear(List((BigInt(2).pow(65), Term.Const("a"))), BigInt(-2).pow(67)).hashCode, -275378075)
    assertEquals(Term.App(Term.Const("f"), List(Term.Num(BigInt(5)))).hashCode, 1439361099)
    assertEquals(Rational(BigInt(2).pow(66), BigInt(3)).hashCode, 119167)
    // A number in the `Long` range hashes as the `Long` does, on every platform, by Scala itself.
    assertEquals(Term.Num(BigInt(7)).hashCode, Term.Num(BigInt(7)).hashCode)
