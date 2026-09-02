package net.bovel.bench.evaluation

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized
import scala.io.Source

import org.openjdk.jmh.annotations.*
import com.microsoft.z3.{Context, Status}
import io.github.cvc5.Context as Cvc5Context
import net.bovel.smtlib.{Corpus, Parser}
import net.bovel.vik.{Lowering, Printer, Script as VikScript, Session}

/** In-process comparison on a parsed script, same JVM, same thread, no process
 *  start-up: `vik`, `z3Direct`, `cvc5Direct`, `princess` and `smtinterpol`
 *  replay the script's own terms through each solver's API — a [[Replay]],
 *  compiled from the instance up front, so an operation is only API calls, and
 *  what the solver receives is what its own parser would have built (checked by
 *  `replaycheck`); `vikText`, `z3` and `cvc5` receive the file as it is and
 *  parse it themselves (`print` measures the printing alone). z3 and cvc5 run
 *  through their Java bindings as published by SoSy-Lab (JavaSMT) with the
 *  native libraries of `fetch-native.sh`. z3 keeps one `Context` for the trial,
 *  as a tool that embeds it does, and takes a fresh solver per operation, reset
 *  on the way out (see [[Z3Replay.check]]): creating a context costs 0.46 ms,
 *  more than the small instances take to solve. cvc5 gets a fresh `Solver` per
 *  operation: the solver itself is cheap (30 µs), and the ~0.4 ms its engine
 *  spends initialising at the first `assert` is what a solver kept in
 *  incremental mode would save — at the price of another engine (24× slower on
 *  `NEQ004_size4`), so the fresh solver stays.
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
      "lustre__two_counters.bmc_k100.smt2",
      "lustre__two_counters.bmc_k100.prefix25.smt2",
      "lustre__ex3.bmc_k100.smt2",
      "lustre__ex3.bmc_k100.prefix25.smt2",
      "2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max.smt2",
      "storeinv__storeinv_t2_np_nf_ai_00001_001.cvc.smt2",
      "swap__swap_invalid_t3_np_nf_ai_00002_009.cvc.smt2",
      "check__array_incompleteness1.smt2",
      "keymaera__division_dijkstra-node701.smt2",
      "Chua__1__VC2__L__Chua-1-VC2-L-chunk-0005.smt2",
      "RandomDecoupled__pb_real_30_60_45_28.smt2",
      "wisas__xs_5_5.smt2",
      "wisas__xs_8_8.smt2",
      "wisas__xs_20_20.smt2",
      "mathsat__Wisa__xs-09-05-2-4-4-5.smt2",
      "mathsat__Wisa__xs-07-11-1-1-1-1.smt2",
      "mathsat__Hash__hash_uns_04_18.smt2",
      "kind__car_6_e2_893.ec.smt2",
      "20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA.smt2",
      "cpachecker__diskperf_simpl1.smt2",
      "cpachecker__list_false-unreach-call.smt2",
      "bmc__stack_k20.smt2",
      "safari__mux.smt2",
      "safari__illinois.smt2"
    )
  )
  var file: String = uninitialized

  private var script: VikScript = uninitialized
  private var text: String = uninitialized
  private var ctx: Context = uninitialized // z3's, for the trial
  private var logic: String | Null = uninitialized // the script's `set-logic`, for z3's solver
  private var replay: Replay | Null = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    text = java.nio.file.Files.readString(Corpus.find(file)) // the file as it is, for the solvers that parse
    val parsed = Parser.parse(text)
    script = Lowering(parsed) // the tree as VIK plays it
    // A script no replay encodes (sort parameters, say) leaves it null; the replays are simply not
    // selected for such instances — the setup must not fail the others.
    replay = try Replay.compile(parsed)
    catch case _: Exception => null
    Natives.z3
    ctx = Context()
    logic = OtherSolversBench.logicOf(text)

  @TearDown(Level.Trial)
  def tearDown(): Unit = ctx.close()

  @Benchmark
  def print(): Int = Printer.print(script).length

  /** Replays the instance — declarations, terms, asserts, `push`/`pop` and
   *  every `check-sat` through VIK's `Session` API, nothing else: what a tool
   *  that builds the terms itself pays, the lowering included.
   */
  @Benchmark
  def vik(): List[Boolean] = Budget.run("vik", file)(VikReplay.check(replay.nn))

  /** The same, from the text of the script: what the solvers that parse below
   *  are measured on.
   */
  @Benchmark
  def vikText(): List[Boolean] = Budget.run("vikText", file)(Session().run(Parser.parse(text)).map(_ == "sat"))

  /** Replays the instance — declarations, terms, asserts, `push`/`pop` and
   *  every `check-sat` through princess's API, nothing else.
   */
  @Benchmark
  def princess(): List[Boolean] = Budget.run("princess", file)(PrincessReplay.check(replay.nn))

  /** Has z3 parse the text (declarations included); the parsed assertions go to
   *  a fresh solver for the script's logic — z3's parser does not pass
   *  `set-logic` on, and the solver for a logic is what the binary runs.
   */
  @Benchmark
  def z3(): Boolean = Budget.run("z3", file):
    val solver = Z3Replay.solver(ctx, logic)
    try
      solver.add(ctx.parseSMTLIB2String(text, null, null, null, null)*)
      solver.check() == Status.SATISFIABLE
    finally solver.reset() // see Z3Replay.check

  /** Replays the instance — declarations, terms, asserts, `push`/`pop` and
   *  every `check-sat` through z3's Java API, nothing else.
   */
  @Benchmark
  def z3Direct(): List[Boolean] = Budget.run("z3Direct", file)(Z3Replay.check(replay.nn, ctx))

  /** Has cvc5 run the text as a script through its parser, each command as its
   *  binary would, recording the check-sat answers. The binding keeps every
   *  object it created until `Context.deletePointers()`: part of the operation,
   *  as it is of every use of the binding.
   */
  @Benchmark
  def cvc5(): List[Boolean] = Budget.run("cvc5", file):
    try TextOracle.cvc5(text)
    finally Cvc5Context.deletePointers()

  /** Replays the instance — declarations, terms, asserts, `push`/`pop` and
   *  every `check-sat` through cvc5's Java API, nothing else.
   */
  @Benchmark
  def cvc5Direct(): List[Boolean] = Budget.run("cvc5Direct", file):
    try Cvc5Replay.check(replay.nn)
    finally Cvc5Context.deletePointers()

  /** Replays the instance — declarations, terms, asserts, `push`/`pop` and
   *  every `check-sat` through the `Script` interface Ultimate embeds, nothing
   *  else.
   */
  @Benchmark
  def smtinterpol(): List[Boolean] = Budget.run("smtinterpol", file)(SmtInterpolReplay.check(replay.nn))

object OtherSolversBench:
  /** The logic a script sets, or null. */
  def logicOf(text: String): String | Null =
    "\\(set-logic (\\S+)\\)".r.findFirstMatchIn(text).map(_.group(1)).orNull
