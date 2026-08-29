package net.bovel.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.util.Random

import org.openjdk.jmh.annotations.*
import net.bovel.cdclt.{Lit, Solver}

/** Pure SAT workloads: each benchmark builds and solves fresh instances. */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
class CdcltBench:

  @Param(Array("30", "60"))
  var n: Int = uninitialized

  /** Solves ten random `k`-SAT instances with `n` variables and `ratio * n`
   *  clauses; returns how many are sat.
   */
  private def randomKSat(k: Int, ratio: Double): Int =
    val rnd = Random(1)
    var sats = 0
    for _ <- 0 until 10 do
      val s = Solver(n)
      for _ <- 0 until (ratio * n).toInt do s.addClause(Array.fill(k)(Lit(rnd.nextInt(n), rnd.nextBoolean())))
      s.solve() match
        case Solver.Result.Sat(_) => sats += 1
        case Solver.Result.Unsat => ()
    sats

  /** Ten random 3-SAT instances at the ratio 4.2, near the threshold: a mix of
   *  sat and unsat.
   */
  @Benchmark
  def random3Sat(): Int = randomKSat(3, 4.2)

  /** Ten random 5-SAT instances at the ratio 20, near the threshold: long
   *  clauses, many per variable.
   */
  @Benchmark
  def random5Sat(): Int = randomKSat(5, 15)

  /** The pigeonhole principle for `n / 10` pigeons and one hole less: unsat,
   *  and hard for resolution.
   */
  @Benchmark
  def pigeonhole(): Boolean =
    val pigeons = n / 8
    val holes = pigeons - 1
    val s = Solver(pigeons * holes)
    def at(p: Int, h: Int) = Lit(p * holes + h)
    for p <- 0 until pigeons do s.addClause(Array.tabulate(holes)(h => at(p, h)))
    for h <- 0 until holes; p <- 0 until pigeons; q <- p + 1 until pigeons do
      s.addClause(at(p, h).neg, at(q, h).neg)
    s.solve() match
      case Solver.Result.Sat(_) => true
      case Solver.Result.Unsat => false
