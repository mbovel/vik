package net.bovel.bench.evaluation

import java.util.concurrent.TimeUnit

import scala.compiletime.uninitialized

import org.openjdk.jmh.annotations.*
import net.bovel.smtlib.Parser
import net.bovel.vik.Session

/** VIK on the evaluation's instances, in process: `api` replays the script's
 *  own terms through a `Session` — declarations, terms, asserts, `push`/`pop`
 *  and every `check-sat`, what a tool that builds the terms itself pays, the
 *  lowering included — and `text` parses the file first, what the solvers that
 *  parse are measured on. Every operation runs under the ten-second [[Budget]].
 */
@State(Scope.Thread)
@BenchmarkMode(Array(Mode.AverageTime))
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(3)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 3, time = 1)
class VikBench:
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

  @Setup(Level.Trial)
  def setup(): Unit = instance = Instance(file)

  @Benchmark
  def api(): List[Boolean] = Budget.run("VikBench.api", file)(VikReplay.check(instance.replay))

  @Benchmark
  def text(): List[Boolean] =
    Budget.run("VikBench.text", file)(Session().run(Parser.parse(instance.text)).map(_ == "sat"))
