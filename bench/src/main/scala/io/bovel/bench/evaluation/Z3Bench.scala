package io.bovel.bench.evaluation

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized

import org.openjdk.jmh.annotations.*
import com.microsoft.z3.{Context, Status}

/** z3 through its Java binding (SoSy-Lab's JavaSMT build, the native library of
 *  `fetch-native.sh`), one `Context` per trial as a tool that embeds it keeps
 *  one — a context costs 0.46 ms, more than the small instances take — and a
 *  fresh solver per operation, reset on the way out (see [[Z3Replay.check]]).
 *  `api` replays the script's own terms through the API, checked against z3's
 *  parser by `replaycheck`.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
class Z3Bench:
  @Param(
    Array(
      "QG-classification__qg5__iso_brn001.smt2",
      "QG-classification__qg5__iso_icl662.smt2",
      "2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2",
      "20170829-Rodin__smt3534838651727683253.smt2",
      "20190906-CLEARSY__0016__00003.smt2",
      "TypeSafe__z3.1184131.smt2",
      "NEQ__NEQ004_size4.smt2",
      "CAV_2009_benchmarks__20-vars__problem__001.smt2",
      "cut_lemmas__10-vars__cut_lemma_01_001.smt2",
      "dillig__20-4.smt2",
      "prime-cone__prime_cone_unsat_3.smt2",
      "convert__convert-tiff2jpg-query-1831.smt2",
      "mathsat__FISCHER1-1-fair.smt2",
      "wisas__xs_8_8.smt2",
      "TwoSquares__smtlib.598294.smt2",
      "mathsat__Wisa__xs-09-05-2-4-4-5.smt2",
      "keymaera__division_dijkstra-node701.smt2",
      "Chua__1__VC2__L__Chua-1-VC2-L-chunk-0005.smt2",
      "RandomDecoupled__pb_real_30_60_45_28.smt2",
      "sage__app7__bench_8505.smt2",
      "bmc-bv__adpcm.smt2",
      "bench_ab__a649test0047.smt2",
      "sage__app2__bench_512.smt2",
      "spear__cvs_v1.11.22__cvs_vc71402.smt2",
      "20210219-Sydr__symbolic_memory__nested__cjpeg__predicate_150.smt2",
      "pspace__ndist.a.20000.smt2",
      "20172804-Barrett__tests__v1__v1l20086.cvc.smt2",
      "20172804-Barrett__tests__v10__v10l20080.cvc.smt2",
      "20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2",
      "20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA.smt2",
      "storeinv__storeinv_t2_np_nf_ai_00001_001.cvc.smt2",
      "swap__swap_invalid_t3_np_nf_ai_00002_009.cvc.smt2",
      "check__array_incompleteness1.smt2",
      "20190906-CLEARSY__0020__00009.smt2",
      "lustre__two_counters.bmc_k100.prefix25.smt2",
      "lustre__ex3.bmc_k100.prefix25.smt2",
      "kind__MESI_i4.ec.smt2",
      "kind__car_6_e2_893.ec.smt2",
      "cpachecker__diskperf_simpl1.smt2",
      "cpachecker__list_false-unreach-call.smt2",
      "2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max.smt2",
      "bmc__stack_k20.smt2",
      "safari__mux.smt2",
      "safari__illinois.smt2"
    )
  )
  var file: String = uninitialized
  private var instance: Instance = uninitialized
  private var ctx: Context = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    instance = Instance(file)
    Natives.z3
    ctx = Context()

  @TearDown(Level.Trial)
  def tearDown(): Unit = ctx.close()

  @Benchmark
  def api(): List[Boolean] = Budget.run("Z3Bench.api", file, Nil)(Z3Replay.check(instance.replay, ctx))

/** z3 parsing the text itself: the parsed assertions go to a fresh solver for
 *  the script's logic (z3's parser does not pass `set-logic` on, and the solver
 *  for a logic is what the binary runs). The parser takes a set of assertions,
 *  so a script of several `check-sat`s under `push`/`pop` is another question
 *  here and sits out: those instances are commented below, and [[Z3Bench]]
 *  replays them through the API.
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
class Z3TextBench:
  @Param(
    Array(
      "QG-classification__qg5__iso_brn001.smt2",
      "QG-classification__qg5__iso_icl662.smt2",
      "2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2",
      "20170829-Rodin__smt3534838651727683253.smt2",
      "20190906-CLEARSY__0016__00003.smt2",
      "TypeSafe__z3.1184131.smt2",
      "NEQ__NEQ004_size4.smt2",
      "CAV_2009_benchmarks__20-vars__problem__001.smt2",
      "cut_lemmas__10-vars__cut_lemma_01_001.smt2",
      "dillig__20-4.smt2",
      "prime-cone__prime_cone_unsat_3.smt2",
      "convert__convert-tiff2jpg-query-1831.smt2",
      "mathsat__FISCHER1-1-fair.smt2",
      "wisas__xs_8_8.smt2",
      "TwoSquares__smtlib.598294.smt2",
      "mathsat__Wisa__xs-09-05-2-4-4-5.smt2",
      "keymaera__division_dijkstra-node701.smt2",
      "Chua__1__VC2__L__Chua-1-VC2-L-chunk-0005.smt2",
      "RandomDecoupled__pb_real_30_60_45_28.smt2",
      "sage__app7__bench_8505.smt2",
      "bmc-bv__adpcm.smt2",
      "bench_ab__a649test0047.smt2",
      "sage__app2__bench_512.smt2",
      "spear__cvs_v1.11.22__cvs_vc71402.smt2",
      "20210219-Sydr__symbolic_memory__nested__cjpeg__predicate_150.smt2",
      "pspace__ndist.a.20000.smt2",
      "20172804-Barrett__tests__v1__v1l20086.cvc.smt2",
      "20172804-Barrett__tests__v10__v10l20080.cvc.smt2",
      "20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2",
      "20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA.smt2",
      "storeinv__storeinv_t2_np_nf_ai_00001_001.cvc.smt2",
      "swap__swap_invalid_t3_np_nf_ai_00002_009.cvc.smt2",
      "check__array_incompleteness1.smt2"
      // "20190906-CLEARSY__0020__00009.smt2", // incremental: the parser flattens the script
      // "lustre__two_counters.bmc_k100.prefix25.smt2", // incremental: the parser flattens the script
      // "lustre__ex3.bmc_k100.prefix25.smt2", // incremental: the parser flattens the script
      // "kind__MESI_i4.ec.smt2", // incremental: the parser flattens the script
      // "kind__car_6_e2_893.ec.smt2", // incremental: the parser flattens the script
      // "cpachecker__diskperf_simpl1.smt2", // incremental: the parser flattens the script
      // "cpachecker__list_false-unreach-call.smt2", // incremental: the parser flattens the script
      // "2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max.smt2", // incremental: the parser flattens the script
      // "bmc__stack_k20.smt2", // incremental: the parser flattens the script
      // "safari__mux.smt2", // incremental: the parser flattens the script
      // "safari__illinois.smt2", // incremental: the parser flattens the script
    )
  )
  var file: String = uninitialized
  private var instance: Instance = uninitialized
  private var ctx: Context = uninitialized

  @Setup(Level.Trial)
  def setup(): Unit =
    instance = Instance(file)
    Natives.z3
    ctx = Context()

  @TearDown(Level.Trial)
  def tearDown(): Unit = ctx.close()

  @Benchmark
  def text(): Boolean = Budget.run("Z3TextBench.text", file, false):
    val solver = Z3Replay.solver(ctx, instance.logic)
    try
      solver.add(ctx.parseSMTLIB2String(instance.text, null, null, null, null)*)
      solver.check() == Status.SATISFIABLE
    finally solver.reset() // see Z3Replay.check
