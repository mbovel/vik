package net.bovel.lia.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.util.Random

import org.openjdk.jmh.annotations.*
import net.bovel.lia.{Solver, Var}

/** Random systems of bounds on linear expressions over `n` integer variables in
 *  the box `[-10, 10]`: `2n` expressions of two or three terms with
 *  coefficients in `[-3, 3]`, each bounded on one side by a constant in
 *  `[-10, 10]` (a mix of feasible and infeasible systems).
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
class SolverBench:

  @Param(Array("10", "20"))
  var n: Int = uninitialized

  /** A bound on an expression: terms, upper or lower, constant. */
  case class Constraint(terms: List[(BigInt, Int)], isUpper: Boolean, c: BigInt)

  private var systems: List[List[Constraint]] = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    val rnd = Random(1)
    systems = List.fill(10):
      List.fill(2 * n):
        val terms = rnd.shuffle((0 until n).toList).take(2 + rnd.nextInt(2)).map(x =>
          (BigInt(1 + rnd.nextInt(3)) * (if rnd.nextBoolean() then 1 else -1), x)
        )
        Constraint(terms, rnd.nextBoolean(), rnd.nextInt(21) - 10)

  private def boxed(solver: Solver): IndexedSeq[Var] = (0 until n).map: _ =>
    val x = solver.newVar()
    solver.assertLower(x, -10, -1)
    solver.assertUpper(x, 10, -1)
    x

  private def assertConstraint(solver: Solver, vars: IndexedSeq[Var], k: Constraint, reason: Int): Solver.Result =
    val s = solver.newSlack(k.terms.map((a, x) => (a, vars(x))))
    if k.isUpper then solver.assertUpper(s, k.c, reason) else solver.assertLower(s, k.c, reason)

  /** Builds and decides each system in one go. */
  @Benchmark
  def solve(): Int =
    var sat = 0
    for system <- systems do
      val solver = Solver()
      val vars = boxed(solver)
      var result: Solver.Result = Solver.Result.Sat
      for (k, i) <- system.zipWithIndex if result == Solver.Result.Sat do result = assertConstraint(solver, vars, k, i)
      if result == Solver.Result.Sat && solver.check() == Solver.Result.Sat then sat += 1
    sat

  /** Asserts the constraints of each system one at a time, deciding after each
   *  (the incremental use of a CDCL(T) solver), until the first infeasible.
   */
  @Benchmark
  def incremental(): Int =
    var checks = 0
    for system <- systems do
      val solver = Solver()
      val vars = boxed(solver)
      var result: Solver.Result = Solver.Result.Sat
      for (k, i) <- system.zipWithIndex if result == Solver.Result.Sat do
        result = assertConstraint(solver, vars, k, i)
        if result == Solver.Result.Sat then result = solver.check()
        checks += 1
    checks

  /** On the first half of each system, tries each constraint of the second half
   *  alone, between a `push` and a `pop`.
   */
  @Benchmark
  def backtrack(): Int =
    var sat = 0
    for system <- systems do
      val solver = Solver()
      val vars = boxed(solver)
      val (base, tries) = system.splitAt(n)
      var result: Solver.Result = Solver.Result.Sat
      for (k, i) <- base.zipWithIndex if result == Solver.Result.Sat do result = assertConstraint(solver, vars, k, i)
      if result == Solver.Result.Sat && solver.check() == Solver.Result.Sat then
        for (k, i) <- tries.zipWithIndex do
          solver.push()
          if assertConstraint(solver, vars, k, n + i) == Solver.Result.Sat && solver.check() == Solver.Result.Sat then
            sat += 1
          solver.pop()
    sat
