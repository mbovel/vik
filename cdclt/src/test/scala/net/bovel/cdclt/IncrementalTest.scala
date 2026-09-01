package net.bovel.cdclt

/** The incremental face of the solver: solving repeatedly, adding clauses and
 *  variables between solves, and assumptions — `Unsat` under assumptions leaves
 *  the clauses satisfiable without them.
 */
class IncrementalTest extends munit.FunSuite:
  private def sat(r: Solver.Result): Boolean = r match
    case Solver.Result.Sat(_) => true
    case _ => false

  test("solve, learn, solve again"):
    val s = Solver(2)
    s.addClause(Lit(0), Lit(1))
    assert(sat(s.solve()))
    s.addClause(Lit(0, false))
    assert(sat(s.solve()))
    s.addClause(Lit(1, false))
    assertEquals(s.solve(), Solver.Result.Unsat)
    assertEquals(s.solve(), Solver.Result.Unsat) // unsat stays unsat

  test("variables added between solves"):
    val s = Solver(1)
    s.addClause(Lit(0))
    assert(sat(s.solve()))
    val v = s.newVar()
    val w = s.newVar()
    s.addClause(Lit(v), Lit(w))
    s.addClause(Lit(v, false), Lit(w, false))
    val model = s.solve() match
      case Solver.Result.Sat(m) => m
      case other => fail(s"expected sat, got $other")
    assert(model(0))
    assert(model(v) != model(w))

  test("assumptions decide, and do not stick"):
    val s = Solver(3)
    s.addClause(Lit(0), Lit(1))
    s.addClause(Lit(0, false), Lit(2))
    // Under a = ¬x2 ∧ ¬x1: forced x0, then x2 — contradiction with ¬x2.
    assertEquals(s.solve(Array(Lit(2, false), Lit(1, false)), () => true), Solver.Result.Unsat)
    // Without the assumptions the clauses are satisfiable, and under the opposite ones too.
    assert(sat(s.solve()))
    s.solve(Array(Lit(2), Lit(1)), () => true) match
      case Solver.Result.Sat(m) => assert(m(2) && m(1))
      case other => fail(s"expected sat, got $other")

  test("an assumption already implied gets its empty level"):
    val s = Solver(2)
    s.addClause(Lit(0)) // a unit: x0 is true at level 0
    s.solve(Array(Lit(0), Lit(1)), () => true) match
      case Solver.Result.Sat(m) => assert(m(0) && m(1))
      case other => fail(s"expected sat, got $other")

  test("a second solve decides every variable again"):
    // Unwinding must move the decision search back before every unassigned
    // variable. When it does not, a variable early in the order is never
    // decided again, and the second model leaves x0 and x1 false — violating
    // the only clause.
    val s = Solver(3)
    s.addClause(Lit(0), Lit(1))
    assert(sat(s.solve()))
    s.solve() match
      case Solver.Result.Sat(m) => assert(m(0) || m(1))
      case other => fail(s"expected sat, got $other")

  test("every model of a solving sequence satisfies every clause"):
    // Random instances solved repeatedly, clauses and variables arriving
    // between solves: whatever the search order does, `Sat` must mean a model.
    val random = scala.util.Random(20260901)
    for _ <- 1 to 100 do
      val s = Solver(4)
      var vars = 4
      val clauses = scala.collection.mutable.ArrayBuffer.empty[Array[Lit]]
      for _ <- 1 to 4 do
        if random.nextInt(3) == 0 then vars = s.newVar() + 1
        for _ <- 1 to 1 + random.nextInt(4) do
          val clause = Array.fill(1 + random.nextInt(3))(Lit(random.nextInt(vars), random.nextBoolean()))
          clauses += clause
          s.addClause(clause)
        s.solve() match
          case Solver.Result.Sat(m) =>
            for c <- clauses do assert(c.exists(l => m(l.variable) == l.positive), s"clause ${c.toList} is false")
          case _ => () // unsat: fine, and it stays so — keep adding, the solver must keep answering
    ()
