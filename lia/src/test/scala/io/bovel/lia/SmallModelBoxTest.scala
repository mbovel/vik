package io.bovel.lia

/** The small-model box prunes a branch past where a solution could be, so it
 *  must take every bound of the system into account, including the ones
 *  asserted after it was cached (2026-09-05, from the soundness audit).
 */
class SmallModelBoxTest extends munit.FunSuite:
  test("a bound with a large constant after a check that pivoted nothing"):
    val s = Solver()
    val x = s.newVar()
    val y = s.newVar()
    val e = s.newSlack(List((BigInt(3), x), (BigInt(-2), y)))
    assertEquals(s.assertUpper(e, 5, 1), Solver.Result.Sat)
    assertEquals(s.assertLower(x, 0, 2), Solver.Result.Sat)
    assertEquals(s.check(), Solver.Result.Sat) // the zero assignment satisfies it: no pivot, the box is cached
    assertEquals(s.assertLower(e, 1, 3), Solver.Result.Sat)
    assertEquals(s.assertUpper(e, 1, 4), Solver.Result.Sat)
    assertEquals(s.assertLower(x, BigInt(10).pow(40), 5), Solver.Result.Sat)
    assertEquals(s.check(), Solver.Result.Sat, "3x - 2y = 1 with x >= 10^40: x = 10^40 + 1")

  test("a disequality on a slack with a large scale"):
    val s = Solver()
    val a = s.newVar()
    val b = s.newVar()
    val e = s.newSlack(List((BigInt(2).pow(100), a), (BigInt(2).pow(100), b)))
    assertEquals(s.assertDistinct(e, 0, 1), Solver.Result.Sat)
    assertEquals(s.check(), Solver.Result.Sat, "2^100 (a + b) != 0: a = 1")

  test("a variable added after the box was cached"):
    val s = Solver()
    val x = s.newVar()
    assertEquals(s.assertLower(x, 0, 1), Solver.Result.Sat)
    assertEquals(s.check(), Solver.Result.Sat)
    val y = s.newVar()
    val e = s.newSlack(List((BigInt(3), x), (BigInt(-2), y)))
    assertEquals(s.assertLower(e, 1, 2), Solver.Result.Sat)
    assertEquals(s.assertUpper(e, 1, 3), Solver.Result.Sat)
    assertEquals(s.assertLower(x, BigInt(10).pow(40), 4), Solver.Result.Sat)
    assertEquals(s.check(), Solver.Result.Sat)
