package net.bovel.smt.bench

import java.io.StringReader
import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.io.Source

import org.openjdk.jmh.annotations.*
import ap.api.SimpleAPI
import de.uni_freiburg.informatik.ultimate.logic.{FunctionSymbol, Script, Term, WrapperScript}
import de.uni_freiburg.informatik.ultimate.smtinterpol.DefaultLogger
import de.uni_freiburg.informatik.ultimate.smtinterpol.option.OptionMap
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.{ParseEnvironment, SMTInterpol}
import net.bovel.smt2.{Parser, Player, Printer, Script as VikScript}

/** In-process comparison on a parsed script: VIK solves the tree directly,
 *  while princess and SMTInterpol receive the tree printed back to SMT-LIB text
 *  and parse it themselves (`print` measures the printing alone). Same JVM,
 *  same thread, no process start-up; z3 and cvc5 have no JVM API here, see
 *  `compare.sh`.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
class OtherSolversBench:

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

  private var script: VikScript = uninitialized
  private var text: String = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    val stream = getClass.getResourceAsStream(s"/smtlib/$file")
    require(stream != null, s"no resource smtlib/$file")
    script = Parser.parse(Source.fromInputStream(stream).mkString)
    text = Printer.print(script)

  @Benchmark
  def print(): Int = Printer.print(script).length

  @Benchmark
  def vik(): List[Boolean] = Player.play(script)

  /** Prints the tree, has princess parse the text into formulas, asserts them
   *  and checks.
   */
  @Benchmark
  def princess(): Boolean =
    val p = SimpleAPI.spawn
    try
      p.extractSMTLIBAssertions(StringReader(Printer.print(script))).foreach(f => p.addAssertion(f))
      p.checkSat(true) == SimpleAPI.ProverStatus.Sat
    finally p.shutDown

  /** A script wrapper recording the answers of `check-sat`. */
  private class Recorder(solver: Script) extends WrapperScript(solver):
    var results: List[Boolean] = Nil
    // Delegations missing from WrapperScript for these two newer Script methods.
    override def getFunctionSymbol(name: String | Null): FunctionSymbol | Null = solver.getFunctionSymbol(name)
    override def getInterpolants(
        partition: Array[Term | Null] | Null,
        startOfSubtree: Array[Int] | Null,
        proofTree: Term | Null
    ): Array[Term | Null] | Null = solver.getInterpolants(partition, startOfSubtree, proofTree)
    override def checkSat(): Script.LBool =
      val result = super.checkSat()
      results ::= (result == Script.LBool.SAT)
      result

  /** Prints the tree and has SMTInterpol run the text as a script, recording
   *  the check-sat answers.
   */
  @Benchmark
  def smtinterpol(): List[Boolean] =
    val solver = SMTInterpol(DefaultLogger())
    solver.setOption(":verbosity", 2)
    val recorder = Recorder(solver)
    ParseEnvironment(
      recorder,
      OptionMap(DefaultLogger(), true)
    ).parseStream(
      StringReader(Printer.print(script)),
      "script"
    ) // front-end options on; `parseScript` takes a file name
    recorder.results.reverse
