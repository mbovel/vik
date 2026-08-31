package net.bovel.cdclt

/** Unsat cores: the failed assumptions of a solve, re-solvable to `Unsat` on
 *  their own.
 */
class CoreTest extends munit.FunSuite:
  test("the core holds the assumptions of the conflict, not the bystanders"):
    val s = Solver(4)
    s.addClause(Lit(0, false), Lit(1)) // x0 -> x1
    s.addClause(Lit(1, false), Lit(2)) // x1 -> x2
    // x0 and !x2 clash through the chain; x3 is a bystander.
    assertEquals(s.solve(Array(Lit(3), Lit(0), Lit(2, false)), () => true), Solver.Result.Unsat)
    val core = s.unsatCore.map(_.variable).toSet
    assert(core.contains(0) && core.contains(2), s"core $core")
    assert(!core.contains(3), s"bystander in core $core")
    // The core alone is still unsat; the bystander alone is not.
    assertEquals(s.solve(s.unsatCore, () => true), Solver.Result.Unsat)
    s.solve(Array(Lit(3)), () => true) match
      case Solver.Result.Sat(_) => ()
      case other => fail(s"expected sat, got $other")

  test("a level-0 unsat has an empty core"):
    val s = Solver(1)
    s.addClause(Lit(0))
    s.addClause(Lit(0, false))
    assertEquals(s.solve(Array(Lit(0)), () => true), Solver.Result.Unsat)
    assert(s.unsatCore.isEmpty)
