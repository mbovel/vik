package net.bovel.smt.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.io.Source

import org.openjdk.jmh.annotations.*
import net.bovel.smt2.{Parser, Player, Script}

/** Solving SMT-LIB QF_UF benchmarks (SMT-LIB release 2025, see
 *  `smt-bench/README.md`): each operation parses and solves one file; `parse`
 *  measures the parsing alone.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(5)
@Warmup(iterations = 10, time = 1)
@Measurement(iterations = 5, time = 1)
class SmtBench:

  @Param(
    Array(
      "eq_diamond__eq_diamond20.smt2",
      "eq_diamond__eq_diamond50.smt2",
      "NEQ__NEQ004_size4.smt2",
      "PEQ__PEQ018_size4.smt2",
      "SEQ__SEQ032_size3.smt2",
      "SEQ__SEQ009_size10.smt2",
      "QG-classification__qg5__iso_brn001.smt2",
      "QG-classification__qg5__iso_icl662.smt2",
      "2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2",
      "2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2",
      "20170829-Rodin__smt3534838651727683253.smt2",
      "20190906-CLEARSY__0016__00003.smt2",
      "TypeSafe__z3.1184131.smt2"
    )
  )
  var file: String = uninitialized

  private var text: String = uninitialized
  private var script: Script = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    val stream = getClass.getResourceAsStream(s"/smtlib/$file")
    require(stream != null, s"no resource smtlib/$file")
    text = Source.fromInputStream(stream).mkString
    script = Parser.parse(text)

  @Benchmark
  def parse(): Int = Parser.parse(text).commands.length

  @Benchmark
  def solve(): List[Boolean] = Player.play(script)
