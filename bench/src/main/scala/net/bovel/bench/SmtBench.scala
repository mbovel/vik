package net.bovel.bench

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.io.Source

import org.openjdk.jmh.annotations.*
import net.bovel.smt2.{Parser, Player, Script}

/** Solving SMT-LIB QF_UF, QF_LIA and QF_BV benchmarks (SMT-LIB release 2025,
 *  see `bench/results/smt/README.md`): each operation parses and solves one
 *  file; `parse` measures the parsing alone. The instances VIK cannot solve
 *  within seconds are left to `compare.sh`.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
class SmtBench:

  @Param(
    Array(
      "NEQ__NEQ004_size4.smt2",
      "PEQ__PEQ018_size4.smt2",
      "SEQ__SEQ032_size3.smt2",
      "QG-classification__qg5__iso_brn001.smt2",
      "QG-classification__qg5__iso_icl662.smt2",
      "2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2",
      "2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2",
      "20170829-Rodin__smt3534838651727683253.smt2",
      "20190906-CLEARSY__0016__00003.smt2",
      "TypeSafe__z3.1184131.smt2",
      "CAV_2009_benchmarks__20-vars__problem__001.smt2",
      "cut_lemmas__10-vars__cut_lemma_01_001.smt2",
      "pidgeons__pigeon-hole-6.smt2",
      "calypto__problem-005675.cvc.2.smt2",
      "dillig__20-4.smt2",
      "mathsat__FISCHER1-1-fair.smt2",
      "rings__ring_2exp4_3vars_2ite_unsat.smt2",
      "prime-cone__prime_cone_unsat_3.smt2",
      "CIRC__multiplier_prime__MULTIPLIER_PRIME_2.msat.smt2",
      "convert__convert-tiff2jpg-query-1831.smt2",
      "sage__app7__bench_8505.smt2",
      "bench_ab__a97test0001.smt2",
      "bmc-bv__adpcm.smt2",
      "bmc-bv__Prim_4.smt2",
      "crafted__bitops7.smt2",
      "crafted__bv8.smt2",
      "2018-Goel-hwbench__QF_BV_bv_bv_swap_two_cc_ref_max.smt2",
      "2018-Goel-hwbench__QF_BV_cav14_example_cc_reg_max.smt2",
      "20190311-bv-term-small-rw-Noetzli__bv-term-small-rw_17.smt2",
      "pspace__ndist.a.20000.smt2"
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
