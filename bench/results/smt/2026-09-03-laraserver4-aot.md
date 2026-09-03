# The JVM with a JDK 25 AOT cache, cold, 2026-09-03, laraserver4

JEP 514 (JDK 25): one training run records the classes loaded and linked and the profiles, and writes an
AOT cache the next runs start from. `bench/aot.sh` trains on the evaluation's instances — one process
solving all 42, `smt2` taking several files for it — and writes `target/aot/vik.aot` (25 MB) with the
classpath it holds for; `bench/cold.sh` measures it as `vik-jvm-aot`. This run: the evaluation's
instances, three repetitions, the median, the ten-second budget; laraserver4 (shared, a CI runner busy on
it), Temurin 25.0.3, Scala 3.9.0. Raw data: [2026-09-03-laraserver4-aot.tsv](2026-09-03-laraserver4-aot.tsv)
(the whole table, the GraalVM image and the other solvers included).

**Result: the cache halves the JVM's cold time.** Over the 41 instances both answered, the JVM without the
cache is 1.93× slower (geometric mean); the median goes from 327 ms to 163 ms, the floor from 223 ms to
89 ms. It stays a JVM start: the GraalVM image ([its record](2026-09-03-laraserver4-graal.md)) answers the
same instances in 6–30 ms, Scala Native in 10–170 ms. The answers agree everywhere. Worth keeping in mind
for a client that starts a JVM per query; for the compiler, which keeps one JVM, the warm numbers are the
ones that matter.

| Instance | VIK (JVM) | VIK (JVM, AOT cache) | VIK (GraalVM Native Image) | VIK (native) | z3 |
|---|---:|---:|---:|---:|---:|
| `QG-classification__qg5__iso_brn001` | 0.33 | 0.17 | 0.01 | 0.05 | 0.02 |
| `QG-classification__qg5__iso_icl662` | 0.42 | 0.21 | 0.03 | 0.17 | 0.02 |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | 0.22 | 0.09 | 0.01 | 0.01 | 0.01 |
| `20170829-Rodin__smt3534838651727683253` | 0.25 | 0.10 | 0.01 | 0.01 | 0.01 |
| `20190906-CLEARSY__0016__00003` | 0.27 | 0.12 | 0.01 | 0.02 | 0.02 |
| `TypeSafe__z3.1184131` | 0.26 | 0.10 | 0.01 | 0.01 | 0.01 |
| `NEQ__NEQ004_size4` | 0.59 | 0.51 | 0.37 | 1.71 | 0.02 |
| `CAV_2009_benchmarks__20-vars__problem__001` | 0.31 | 0.14 | 0.01 | 0.03 | 0.02 |
| `cut_lemmas__10-vars__cut_lemma_01_001` | 0.26 | 0.11 | 0.01 | 0.01 | 0.02 |
| `dillig__20-4` | 0.33 | 0.17 | 0.03 | 0.07 | 0.02 |
| `prime-cone__prime_cone_unsat_3` | 0.27 | 0.11 | 0.01 | 0.01 | 0.02 |
| `convert__convert-tiff2jpg-query-1831` | 0.30 | 0.14 | 0.01 | 0.03 | 0.02 |
| `mathsat__FISCHER1-1-fair` | 0.30 | 0.14 | 0.01 | 0.03 | 0.02 |
| `wisas__xs_8_8` | 0.50 | 0.28 | 0.09 | 0.31 | 0.02 |
| `TwoSquares__smtlib.598294` | 0.27 | 0.11 | 0.01 | 0.01 | 0.02 |
| `mathsat__Wisa__xs-09-05-2-4-4-5` | 0.51 | 0.30 | 0.10 | 0.35 | 0.02 |
| `keymaera__division_dijkstra-node701` | 0.23 | 0.09 | 0.01 | 0.01 | 0.01 |
| `Chua__1__VC2__L__Chua-1-VC2-L-chunk-0005` | 0.26 | 0.11 | 0.01 | 0.01 | 0.01 |
| `RandomDecoupled__pb_real_30_60_45_28` | 0.60 | 0.30 | 0.09 | 0.65 | 0.02 |
| `bench_ab__a649test0047` | 0.43 | 0.22 | 0.07 | 0.71 | 0.02 |
| `sage__app2__bench_512` | 0.50 | 0.32 | 0.17 | 1.27 | 0.08 |
| `pspace__ndist.a.20000` | 0.27 | 0.11 | 0.01 | 0.02 | 🐢 |
| `20172804-Barrett__tests__v1__v1l20086.cvc` | 0.26 | 0.12 | 0.01 | 0.01 | 0.01 |
| `20172804-Barrett__tests__v10__v10l20080.cvc` | 0.28 | 0.12 | 0.01 | 0.01 | 0.02 |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0` | 0.31 | 0.14 | 0.01 | 0.01 | 0.02 |
| `20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA` | 🐢 | 🐢 | 🐢 | 🐢 | 0.04 |
| `alpha-093dddceea58` | 0.32 | 0.14 | 0.01 | 0.01 | 0.02 |
| `alpha-fib_memoized-36bd61c61979` | 0.34 | 0.16 | 0.02 | 0.05 | 0.02 |
| `storeinv__storeinv_t2_np_nf_ai_00001_001.cvc` | 0.27 | 0.11 | 0.01 | 0.01 | 0.02 |
| `swap__swap_invalid_t3_np_nf_ai_00002_009.cvc` | 0.29 | 0.13 | 0.01 | 0.01 | 0.01 |
| `check__array_incompleteness1` | 0.34 | 0.16 | 0.01 | 0.04 | 0.01 |
| `20190906-CLEARSY__0020__00009` | 0.45 | 0.24 | 0.05 | 0.19 | 0.02 |
| `lustre__two_counters.bmc_k100.prefix25` | 0.40 | 0.23 | 0.03 | 0.10 | 0.02 |
| `lustre__ex3.bmc_k100.prefix25` | 1.59 | 1.47 | 1.27 | 3.98 | 0.19 |
| `kind__MESI_i4.ec` | 1.80 | 1.59 | 1.49 | 5.79 | 0.03 |
| `kind__car_6_e2_893.ec` | 4.03 | 3.93 | 5.81 | 🐢 | 0.10 |
| `cpachecker__diskperf_simpl1` | 0.35 | 0.18 | 0.01 | 0.04 | 0.02 |
| `cpachecker__list_false-unreach-call` | 0.88 | 0.72 | 0.62 | 2.34 | 0.03 |
| `2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max` | 0.31 | 0.14 | 0.01 | 0.02 | 0.02 |
| `bmc__stack_k20` | 0.69 | 0.48 | 0.22 | 0.87 | 0.02 |
| `safari__mux` | 0.80 | 0.52 | 0.27 | 1.31 | 0.03 |
| `safari__illinois` | 1.00 | 0.74 | 0.46 | 2.23 | 0.05 |
