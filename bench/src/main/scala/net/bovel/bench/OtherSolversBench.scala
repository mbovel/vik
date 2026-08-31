package net.bovel.bench

import java.io.StringReader
import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.io.Source

import org.openjdk.jmh.annotations.*
import ap.api.SimpleAPI
import com.microsoft.z3.{Context, Status}
import io.github.cvc5.{InputParser, Solver as Cvc5Solver}
import io.github.cvc5.modes.InputLanguage
import de.uni_freiburg.informatik.ultimate.logic.{FunctionSymbol, Script, Term, WrapperScript}
import de.uni_freiburg.informatik.ultimate.smtinterpol.DefaultLogger
import de.uni_freiburg.informatik.ultimate.smtinterpol.option.OptionMap
import de.uni_freiburg.informatik.ultimate.smtinterpol.smtlib2.{ParseEnvironment, SMTInterpol}
import net.bovel.smt2.{Corpus, Parser, Player, Printer, Script as VikScript}

/** In-process comparison on a parsed script, same JVM, same thread, no process
 *  start-up: VIK solves the tree directly; princess, SMTInterpol, z3 and cvc5
 *  receive the file as it is and parse it themselves
 *  (`print` measures the printing alone), and `z3Direct` builds z3's AST from
 *  the tree through the Java API, the way a program would. z3 and cvc5 run
 *  through their Java bindings as published by SoSy-Lab (JavaSMT) with the
 *  native libraries of `fetch-native.sh`; every operation creates and closes
 *  its own context, as the other solvers do. `z3Direct` replays a
 *  [[Z3Program]]: only z3 API calls in the operation, compiled from the
 *  instance up front — for a bit-vector instance the front end lowers to
 *  integers while parsing, so that column measures z3 on the lowered problem.
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
      "pspace__ndist.a.20000.smt2",
      "20172804-Barrett__tests__v1__v1l20086.cvc.smt2",
      "20172804-Barrett__tests__v10__v10l20080.cvc.smt2",
      "20172804-Barrett__tests__v10__v10l60049.cvc.smt2",
      "20172804-Barrett__typed__v3__typed_v3l30025.cvc.smt2",
      "20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2",
      "20230720-blocksworld__from_11_0_0_to_6_4_1_bmc_1.smt2",
      "20230720-blocksworld__from_5_4_17_to_12_6_8_bmc_2.smt2",
      "TwoSquares__smtlib.598294.smt2",
      "TwoSquares__smtlib.602033.smt2",
      "20190906-CLEARSY__0020__00009.smt2",
      "kind__MESI_i4.ec.smt2",
      "wisas__xs_5_5.smt2",
      "wisas__xs_8_8.smt2",
      "wisas__xs_20_20.smt2",
      "mathsat__Wisa__xs-09-05-2-4-4-5.smt2",
      "mathsat__Wisa__xs-07-11-1-1-1-1.smt2",
      "mathsat__Hash__hash_uns_04_18.smt2"
    )
  )
  var file: String = uninitialized

  private var script: VikScript = uninitialized
  private var text: String = uninitialized
  private var z3Program: Z3Program = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    text = java.nio.file.Files.readString(Corpus.find(file)) // the file as it is, for the solvers that parse
    script = Parser.parse(text)
    z3Program = Z3Program.compile(script)

  @Benchmark
  def print(): Int = Printer.print(script).length

  /** The solver on a problem it already holds, as a tool that builds the terms itself calls it. */
  @Benchmark
  def vik(): List[Boolean] = Player.play(script)

  /** The same, from the text of the script: what the solvers that parse below are measured on. */
  @Benchmark
  def vikText(): List[Boolean] = Player.play(Parser.parse(text))

  /** Prints the tree and has princess execute the text as a script —
   *  `push`/`pop` and every `check-sat` included. Princess prints the answers;
   *  they are captured and parsed back, as recording them costs it nothing.
   */
  @Benchmark
  def princess(): List[Boolean] =
    val p = SimpleAPI.spawn
    try
      val buffer = java.io.ByteArrayOutputStream()
      Console.withOut(java.io.PrintStream(buffer)) { p.execSMTLIB(StringReader(text)) }
      buffer.toString.linesIterator.collect {
        case "sat" => true
        case "unsat" => false
      }.toList
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

  /** Prints the tree and has z3 parse it (declarations included); the parsed
   *  assertions go to a fresh solver.
   */
  @Benchmark
  def z3(): Boolean =
    Natives.z3
    val ctx = Context()
    try
      val solver = ctx.mkSolver()
      solver.add(ctx.parseSMTLIB2String(text, null, null, null, null)*)
      solver.check() == Status.SATISFIABLE
    finally ctx.close()

  /** Replays the instance's [[Z3Program]] — declarations, terms, asserts,
   *  `push`/`pop` and every `check-sat` through z3's Java API, nothing else.
   */
  @Benchmark
  def z3Direct(): List[Boolean] =
    Natives.z3
    val ctx = Context()
    try z3Program.check(ctx)
    finally ctx.close()

  /** Prints the tree and has cvc5 run the text as a script through its parser,
   *  recording the check-sat answers.
   */
  @Benchmark
  def cvc5(): List[Boolean] =
    val solver = Cvc5Solver()
    try
      if text.contains("(push") then solver.setOption("incremental", "true") // scripts that push need the option
      val parser = InputParser(solver)
      parser.setStringInput(InputLanguage.SMT_LIB_2_6, text, "script")
      val symbols = parser.getSymbolManager
      var results = List.empty[Boolean]
      var command = parser.nextCommand()
      while !command.isNull do
        val output = command.invoke(solver, symbols)
        if output.startsWith("sat") || output.startsWith("unsat") then results ::= output.startsWith("sat")
        command = parser.nextCommand()
      results.reverse
    finally solver.deletePointer()

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
      StringReader(text),
      "script"
    ) // front-end options on; `parseScript` takes a file name
    recorder.results.reverse
