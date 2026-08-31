package net.bovel.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.util.Random

import org.openjdk.jmh.annotations.*
import net.bovel.sexpr.SExprParser

/** Parsing a generated text of `n` S-expressions, each a nested application
 *  with symbols, numerals, a comment and a quoted symbol now and then.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
class SExprBench:

  @Param(Array("1000", "10000"))
  var n: Int = uninitialized

  private var text: String = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    val rnd = Random(1)
    def expr(depth: Int): String =
      if depth == 0 then
        rnd.nextInt(4) match
          case 0 => s"x${rnd.nextInt(100)}"
          case 1 => rnd.nextInt(1000).toString
          case 2 => s"|quoted ${rnd.nextInt(10)}|"
          case _ => ":keyword"
      else s"(f${rnd.nextInt(10)} ${List.fill(1 + rnd.nextInt(3))(expr(depth - 1)).mkString(" ")})"
    text = (0 until n).map(i => (if i % 10 == 0 then "; a comment\n" else "") + expr(3)).mkString("\n")

  @Benchmark
  def parse(): Int = SExprParser.parse(text).length
