package net.bovel.cdclt.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.util.Random

import org.openjdk.jmh.annotations.*
import net.bovel.cdclt.{Lit, Solver}

/** Pure SAT workloads: each benchmark builds and solves fresh instances. */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(5)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
class SolverBench:

  @Param(Array("30", "60"))
  var n: Int = uninitialized

  /** Ten random 3-SAT instances with `n` variables at the ratio 4.2 (a mix of
   *  sat and unsat).
   */
  @Benchmark
  def random3Sat(): Int =
    val rnd = Random(1)
    var sats = 0
    for _ <- 0 until 10 do
      val s = Solver(n)
      for _ <- 0 until (4.2 * n).toInt do
        s.addClause(
          Lit(rnd.nextInt(n), rnd.nextBoolean()),
          Lit(rnd.nextInt(n), rnd.nextBoolean()),
          Lit(rnd.nextInt(n), rnd.nextBoolean())
        )
      s.solve() match
        case s.Result.Sat(_) => sats += 1
        case s.Result.Unsat => ()
    sats

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
      case s.Result.Sat(_) => true
      case s.Result.Unsat => false
