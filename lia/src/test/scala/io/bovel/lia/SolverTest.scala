package io.bovel.lia

import scala.util.Random

import Solver.Result

class SolverTest extends munit.FunSuite:
  /** A bound on the value of an expression: `Σ coefficients × variables ⋈ c`,
   *  as a test-side description.
   */
  case class Constraint(terms: List[(BigInt, Int)], isUpper: Boolean, c: BigInt, distinct: Boolean = false):
    def holds(assignment: Int => BigInt): Boolean =
      val v = terms.map((a, x) => a * assignment(x)).sum
      if distinct then v != c else if isUpper then v <= c else v >= c

  /** Asserts the constraints with their index as reason, over `n` variables. */
  def solverOf(n: Int, constraints: List[Constraint]): (Solver, Result) =
    val solver = Solver()
    val vars = List.fill(n)(solver.newVar())
    var result: Result = Result.Sat
    for (k, i) <- constraints.zipWithIndex do
      val x = k.terms match
        case (a, x) :: Nil if a == 1 => vars(x)
        case terms => solver.newSlack(terms.map((a, x) => (a, vars(x))))
      val r =
        if k.distinct then solver.assertDistinct(x, k.c, i)
        else if k.isUpper then solver.assertUpper(x, k.c, i)
        else solver.assertLower(x, k.c, i)
      if r != Result.Sat && result == Result.Sat then result = r
    (solver, result)

  def solve(n: Int, constraints: List[Constraint]): (Solver, Result) =
    val (solver, asserted) = solverOf(n, constraints)
    (solver, if asserted == Result.Sat then solver.check() else asserted)

  test("infeasible bounds on a sum"):
    val k = List(
      Constraint(List((1, 0)), false, 1), // x ≥ 1
      Constraint(List((1, 1)), false, 1), // y ≥ 1
      Constraint(List((1, 0), (1, 1)), true, 1) // x + y ≤ 1
    )
    assertEquals(solve(2, k)._2, Result.Unsat(Set(0, 1, 2)))

  test("a feasible system and its model"):
    val k = List(
      Constraint(List((1, 0), (1, 1)), false, 3), // x + y ≥ 3
      Constraint(List((1, 0), (-1, 1)), true, -1), // x - y ≤ -1
      Constraint(List((1, 0)), false, 0), // x ≥ 0
      Constraint(List((1, 1)), true, 3) // y ≤ 3
    )
    val (solver, result) = solve(2, k)
    assertEquals(result, Result.Sat)
    assert(k.forall(_.holds(solver.value)), (solver.value(0), solver.value(1)))

  test("bounds on an expression are tightened by the gcd of its coefficients"):
    val k =
      List(Constraint(List((2, 0), (2, 1)), true, 3), Constraint(List((2, 0), (2, 1)), false, 3)) // 3 ≤ 2x + 2y ≤ 3
    assertEquals(solve(2, k)._2, Result.Unsat(Set(0, 1)))

  test("branch and bound"):
    // x + 2y = 1 and x = 2y: 4y = 1, feasible over the rationals only.
    val k = List(
      Constraint(List((1, 0), (2, 1)), true, 1),
      Constraint(List((1, 0), (2, 1)), false, 1),
      Constraint(List((1, 0), (-2, 1)), true, 0),
      Constraint(List((1, 0), (-2, 1)), false, 0)
    )
    assertEquals(solve(2, k)._2, Result.Unsat(Set(0, 1, 2, 3)))
    // x + 2y = 1 and x ≥ 2y: y = 0, x = 1 among others.
    val (solver, result) = solve(2, k.take(3))
    assertEquals(result, Result.Sat)
    assert(k.take(3).forall(_.holds(solver.value)))

  /** Random constraints over `n` variables in a box, with the brute force over
   *  the box as the oracle.
   */
  def randomConstraints(rnd: Random, n: Int, m: Int, box: Int): List[Constraint] =
    val bounds = (0 until n).flatMap(x =>
      List(Constraint(List((1, x)), false, -box), Constraint(List((1, x)), true, box))
    )
    val others = List.fill(m):
      val terms = rnd.shuffle((0 until n).toList).take(1 + rnd.nextInt(3)).map(x => (BigInt(rnd.nextInt(7) - 3), x))
      Constraint(
        terms.filter(_._1 != 0).map((a, x) => (a, x)) match
          case Nil => List((BigInt(1), rnd.nextInt(n)))
          case t => t
        ,
        rnd.nextBoolean(),
        rnd.nextInt(2 * box + 1) - box,
        rnd.nextInt(4) == 0
      )
    bounds.toList ++ others

  def bruteForce(n: Int, box: Int, constraints: List[Constraint]): Boolean =
    def go(x: Int, assignment: List[BigInt]): Boolean =
      if x == n then constraints.forall(_.holds(assignment.reverse))
      else (-box to box).exists(v => go(x + 1, BigInt(v) :: assignment))
    go(0, Nil)

  test("random systems against brute force"):
    val rnd = Random(7)
    var sat = 0
    var unsat = 0
    for round <- 0 until 300 do
      val n = 2 + rnd.nextInt(3)
      val box = 2
      val k = randomConstraints(rnd, n, 2 + rnd.nextInt(5), box)
      val (solver, result) = solve(n, k)
      val expected = bruteForce(n, box, k)
      result match
        case Result.Sat =>
          assert(expected, s"round $round: sat but brute force finds no solution: $k")
          assert(k.forall(_.holds(solver.value)), s"round $round: model violates a constraint")
          sat += 1
        case Result.Unknown | Result.Split(_, _) => fail(s"round $round: the solver gave up")
        case Result.Unsat(reasons) =>
          assert(!expected, s"round $round: unsat but brute force finds a solution: $k")
          assert(reasons.forall(r => r >= 0 && r < k.length), s"round $round: reasons $reasons")
          // The explanation is unsatisfiable by itself.
          assertEquals(
            solve(n, reasons.toList.sorted.map(k))._2.getClass,
            classOf[Result.Unsat],
            s"round $round: explanation $reasons of $k"
          )
          unsat += 1
    assert(sat > 50 && unsat > 50, (sat, unsat))

  test("push and pop"):
    val rnd = Random(11)
    for round <- 0 until 100 do
      val n = 2 + rnd.nextInt(3)
      val base = randomConstraints(rnd, n, 1 + rnd.nextInt(3), 3)
      val (solver, asserted) = solverOf(n, base)
      if asserted == Result.Sat && solver.check() == Result.Sat then
        val more = randomConstraints(rnd, n, 1 + rnd.nextInt(3), 3).drop(2 * n)
        solver.push()
        var result: Result = Result.Sat
        for (k, i) <- more.zipWithIndex do
          val x = solver.newSlack(k.terms)
          val r =
            if k.distinct then solver.assertDistinct(x, k.c, 100 + i)
            else if k.isUpper then solver.assertUpper(x, k.c, 100 + i)
            else solver.assertLower(x, k.c, 100 + i)
          if r != Result.Sat && result == Result.Sat then result = r
        if result == Result.Sat then result = solver.check()
        assertEquals(result.isInstanceOf[Result.Sat.type], bruteForce(n, 3, base ++ more), s"round $round")
        solver.pop()
        assertEquals(solver.check(), Result.Sat, s"round $round: not satisfiable again after pop")
        assert(base.forall(_.holds(solver.value)), s"round $round: model after pop")

  test("a mixed row confining an integer difference to an empty window is unsat"):
    val solver = Solver()
    val n1 = solver.newVar()
    val n2 = solver.newVar()
    val x = solver.newVar(integer = false)
    // x = n1 - n2, and 0 < x < 1: an integer difference in an open unit interval.
    val s = solver.newSlack(List((BigInt(1), n1), (BigInt(-1), n2), (BigInt(-1), x)), integer = false)
    assertEquals(solver.assertUpper(s, 0, 1), Result.Sat)
    assertEquals(solver.assertLower(s, 0, 2), Result.Sat)
    assertEquals(solver.assertLower(x, 0, true, 3), Result.Sat)
    assertEquals(solver.assertUpper(x, 1, true, 4), Result.Sat)
    solver.check() match
      case Result.Unsat(reasons) => assert(reasons.subsetOf(Set(1, 2, 3, 4)), reasons)
      case other => fail(s"expected unsat, got $other")

  test("the small-model box turns a sliding branch and bound into a sound unsat"):
    // x even and x odd: integer-infeasible, rationally satisfiable everywhere, and no single
    // expression's gcd sees it — the branches slide.
    def parity(): Solver =
      val s = Solver()
      val x = s.newVar()
      val y = s.newVar()
      val z = s.newVar()
      val even = s.newSlack(List((BigInt(1), x), (BigInt(-2), y)))
      val odd = s.newSlack(List((BigInt(1), x), (BigInt(-2), z)))
      assertEquals(s.assertUpper(even, 0, 1), Result.Sat)
      assertEquals(s.assertLower(even, 0, 2), Result.Sat)
      assertEquals(s.assertUpper(odd, 1, 3), Result.Sat)
      assertEquals(s.assertLower(odd, 1, 4), Result.Sat)
      s
    val diverging = parity()
    var steps = 10000
    diverging.keepGoing = () =>
      steps -= 1; steps >= 0
    assertEquals(diverging.check(), Result.Unknown) // without the box: to the fuel
    val boxed = parity()
    boxed.boxBitsOverride = 8 // a test-sized box: past it, exhaustion is unsat
    boxed.check() match
      case Result.Unsat(reasons) => assert(reasons.subsetOf(Set(1, 2, 3, 4)), reasons)
      case other => fail(s"expected unsat, got $other")

  test("splitting on demand hands a split that failed on both sides to the client"):
    // x = 2y and x = 2z + 1 with 0 ≤ y, z ≤ 3: integer-infeasible, and no single expression's gcd sees it.
    def parity(): Solver =
      val s = Solver()
      val x = s.newVar()
      val y = s.newVar()
      val z = s.newVar()
      val even = s.newSlack(List((BigInt(1), x), (BigInt(-2), y)))
      val odd = s.newSlack(List((BigInt(1), x), (BigInt(-2), z)))
      for (v, lo, hi, reason) <- List((even, 0, 0, 1), (odd, 1, 1, 3), (y, 0, 3, 5), (z, 0, 3, 7)) do
        assertEquals(s.assertLower(v, lo, reason), Result.Sat)
        assertEquals(s.assertUpper(v, hi, reason + 1), Result.Sat)
      s
    // Without it, a check answers by itself, however often it is asked.
    val alone = parity()
    for _ <- 0 until 3 do assert(alone.check().isInstanceOf[Result.Unsat])

    val solver = parity()
    solver.splitOnDemand = true
    var splits = 0
    var reason = 100

    /** Refutes the problem as a client would: both sides of every split handed
     *  over, each asserted with a reason of its own. Returns the reasons used.
     */
    def refute(): Set[Int] = solver.check() match
      case Result.Unsat(reasons) => reasons
      case Result.Split(x, c) =>
        splits += 1
        reason += 2
        val (left, right) = (reason, reason + 1)
        solver.push()
        assertEquals(solver.assertUpper(x, c, left), Result.Sat)
        val l = refute()
        solver.pop()
        solver.push()
        assertEquals(solver.assertLower(x, c + 1, right), Result.Sat)
        val r = refute()
        solver.pop()
        (l - left) ++ (r - right)
      case other => fail(s"expected unsat or a split, got $other")
    assert(refute().subsetOf((1 to 8).toSet)) // the first check: nothing failed before, so no split
    assertEquals(splits, 0)
    // A split that failed comes up again within a few checks (the relaxation may start elsewhere): it is
    // the client's then, and the refutation of its two sides is one of the problem.
    var checks = 0
    while splits == 0 && checks < 20 do
      assert(refute().subsetOf((1 to 8).toSet))
      checks += 1
    assert(splits > 0)
