package net.bovel.lia

/** Real variables: the relaxation decides, strict bounds through δ. */
class RealTest extends munit.FunSuite:
  test("a strict window is satisfiable, its closure point is not"):
    val s = Solver()
    val x = s.newVar(integer = false)
    assertEquals(s.assertLower(x, 0, strict = true, reason = 1), Solver.Result.Sat)
    assertEquals(s.assertUpper(x, 1, strict = true, reason = 2), Solver.Result.Sat)
    assertEquals(s.check(), Solver.Result.Sat)
    val v = s.realValue(x)
    assert(v > Rational.zero && v < Rational.one, v.toString)

  test("x < c and x > c together are unsat, naming both"):
    val s = Solver()
    val x = s.newVar(integer = false)
    assertEquals(s.assertUpper(x, 5, strict = true, reason = 1), Solver.Result.Sat)
    s.assertLower(x, 5, strict = true, reason = 2) match
      case Solver.Result.Unsat(reasons) => assertEquals(reasons, Set(1, 2))
      case other => fail(s"expected unsat, got $other")

  test("an integer variable still branches; a real one does not"):
    val s = Solver()
    val x = s.newVar() // integer
    val y = s.newVar(integer = false)
    val sum = s.newSlack(List((BigInt(2), x)))
    assertEquals(s.assertLower(sum, 1, strict = false, reason = 1), Solver.Result.Sat)
    // 2x = 1 has no integer solution: the gcd tightening refuses it at the assert already.
    val refused = s.assertUpper(sum, 1, strict = false, reason = 2) match
      case Solver.Result.Unsat(_) => true
      case Solver.Result.Sat => s.check() match
          case Solver.Result.Unsat(_) => true
          case _ => false
      case _ => false
    assert(refused)
    val s2 = Solver()
    val z = s2.newVar(integer = false)
    val sum2 = s2.newSlack(List((BigInt(2), z)), integer = false)
    assertEquals(s2.assertLower(sum2, 1, strict = false, reason = 1), Solver.Result.Sat)
    assertEquals(s2.assertUpper(sum2, 1, strict = false, reason = 2), Solver.Result.Sat)
    assertEquals(s2.check(), Solver.Result.Sat) // 2z = 1: z = 1/2
    assertEquals(s2.realValue(z), Rational(1, 2))

  test("a real disequality branches strictly"):
    val s = Solver()
    val x = s.newVar(integer = false)
    assertEquals(s.assertLower(x, 3, strict = false, reason = 1), Solver.Result.Sat)
    assertEquals(s.assertUpper(x, 3, strict = false, reason = 2), Solver.Result.Sat)
    assertEquals(s.assertDistinct(x, 3, reason = 3), Solver.Result.Unsat(Set(1, 2, 3)))
