package net.bovel.smt2.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.util.Random

import org.openjdk.jmh.annotations.*
import net.bovel.smt2.{Command, Parser}

/** Parsing a generated QF_UF script with `n` assertions over a few sorts,
 *  functions and constants, using `let`, `ite`, `distinct`, `=>` and `=`.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
class Smt2Bench:

  @Param(Array("1000", "10000"))
  var n: Int = uninitialized

  private var text: String = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    val rnd = Random(1)
    val header =
      "(set-logic QF_UF)\n(declare-sort U 0)\n(declare-fun f (U) U)\n(declare-fun g (U U) U)\n(declare-fun p (U) Bool)\n" +
        (0 until 50).map(i => s"(declare-const c$i U)").mkString("\n")
    def term(depth: Int): String =
      if depth == 0 then s"c${rnd.nextInt(50)}"
      else
        rnd.nextInt(3) match
          case 0 => s"(f ${term(depth - 1)})"
          case 1 => s"(g ${term(depth - 1)} ${term(depth - 1)})"
          case _ => s"(ite (p ${term(depth - 1)}) ${term(depth - 1)} ${term(depth - 1)})"
    def formula(): String = rnd.nextInt(4) match
      case 0 => s"(= ${term(2)} ${term(2)})"
      case 1 => s"(let ((t ${term(2)})) (=> (p t) (= (f t) ${term(1)})))"
      case 2 => s"(distinct ${term(1)} ${term(1)} ${term(1)})"
      case _ => s"(or (not (p ${term(2)})) (= ${term(1)} ${term(1)}))"
    text = header + "\n" + (0 until n).map(_ => s"(assert ${formula()})").mkString("\n") + "\n(check-sat)\n"

  @Benchmark
  def parse(): Int = Parser.parse(text).commands.count(_.isInstanceOf[Command.Assert])
