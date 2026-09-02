package net.bovel.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized

import net.bovel.vik.*
import org.openjdk.jmh.annotations.*
import net.bovel.vik.Formula.*
import net.bovel.vik.Solver
import net.bovel.vik.Term.*

/** Lambdas: `n` function symbols defined by lambdas in a chain,
 *  `g_0 = λx. f(x)` and `g_i = λx. g_{i-1}(x) + 1`, and `g_n(c)` compared with
 *  its unfolding `f(c) + n`: unsatisfiable when different (`unsat`),
 *  satisfiable when equal (`sat`). Every application meets every lambda in the
 *  lemmas, so there are `O(n²)` of them, and their integer terms are shared
 *  with the LIA solver.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
class LambdaBench:

  @Param(Array("10", "30"))
  var n: Int = uninitialized

  private var definitions: List[Formula] = uninitialized
  private var unfolding: Formula = uninitialized

  private def g(i: Int): Term = Const(s"g$i")
  private def plus(t: Term, k: Int): Term = Term.linear(List((BigInt(1), t)), k)

  @Setup
  def setup(): Unit =
    val x = Var(0)
    definitions = Eq(g(0), Lambda(List("Int"), App(Const("f"), List(x)))) ::
      (1 to n).toList.map(i => Eq(g(i), Lambda(List("Int"), plus(App(g(i - 1), List(x)), 1))))
    unfolding = IntEq(App(g(n), List(Const("c"))), plus(App(Const("f"), List(Const("c"))), n))

  private def solver(): Solver =
    val s = Solver()
    s.declare("f", List("Int"), "Int")
    s.declare("c", Nil, "Int")
    for i <- 0 to n do s.declare(s"g$i", List("Int"), "Int")
    definitions.foreach(s.assert)
    s

  @Benchmark
  def unsat(): Boolean =
    val s = solver()
    s.assert(Not(unfolding))
    s.solve() == s.Result.Sat

  @Benchmark
  def sat(): Boolean =
    val s = solver()
    s.assert(unfolding)
    s.solve() == s.Result.Sat
