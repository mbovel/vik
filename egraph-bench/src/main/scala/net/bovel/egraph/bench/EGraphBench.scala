package net.bovel.egraph.bench

import java.util.concurrent.TimeUnit

import scala.collection.immutable.ArraySeq
import scala.collection.mutable.ArrayBuffer
import scala.compiletime.uninitialized
import scala.util.Random

import org.openjdk.jmh.annotations.*

import net.bovel.egraph.{EClassId, EGraph, ENode}

/** Each benchmark builds a fresh e-graph, so construction time is included; `build` measures that part alone. All
  * workloads are linear in `n` except `star`, which is quadratic as long as `rebuild` re-scans the parents of the
  * larger class on every merge.
  */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(5)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
class EGraphBench:

  @Param(Array("1000", "4000"))
  var n: Int = uninitialized

  /** `n` symbol classes, then `2n` binary nodes `h(x, y)` over previously created classes. */
  private def randomDag(eg: EGraph, rnd: Random): ArrayBuffer[EClassId] =
    val h = eg.newClass()
    val ids = ArrayBuffer.fill(n)(eg.newClass())
    for _ <- 0 until 2 * n do
      ids += eg.add(ENode(h, ArraySeq(ids(rnd.nextInt(ids.size)), ids(rnd.nextInt(ids.size)))))
    ids

  /** Hashcons path only. */
  @Benchmark
  def build(): EClassId =
    randomDag(EGraph(), Random(1)).last

  /** egg-style batch: random DAG, `n / 2` random merges, a single `rebuild`. */
  @Benchmark
  def randomBatch(): EClassId =
    val eg = EGraph()
    val ids = randomDag(eg, Random(1))
    val rnd = Random(2)
    for _ <- 0 until n / 2 do eg.merge(ids(rnd.nextInt(ids.size)), ids(rnd.nextInt(ids.size)))
    eg.rebuild()
    eg.find(ids.last)

  /** CDCL(T)-style increments: leaves `x_i` with parents `f(x_i)`; merge `x_i = x_{i+1}` and `rebuild` after each. */
  @Benchmark
  def chain(): EClassId =
    val eg = EGraph()
    val f = eg.newClass()
    val xs = Array.fill(n + 1)(eg.newClass())
    for x <- xs do eg.add(ENode(f, ArraySeq(x)))
    for i <- 0 until n do
      eg.merge(xs(i), xs(i + 1))
      eg.rebuild()
    eg.find(xs(0))

  /** One class `a` with `n` parents `g(a, c_j)`, merged with `n` fresh singletons one at a time, `rebuild` after each. */
  @Benchmark
  def star(): EClassId =
    val eg = EGraph()
    val g = eg.newClass()
    val a = eg.newClass()
    for _ <- 0 until n do eg.add(ENode(g, ArraySeq(a, eg.newClass())))
    for _ <- 0 until n do
      eg.merge(a, eg.newClass())
      eg.rebuild()
    eg.find(a)
