# The solver as a GraalVM native image, cold, 2026-09-03, laraserver4

`bench/graal.sh` builds `target/graal/vik` with GraalVM's `native-image` (`--no-fallback -O2`, GraalVM CE
25.0.2, 16 s, 18 MB) from the solver's runtime classpath; `bench/cold.sh` measures it as `vik-graal` when
it exists. This run: the evaluation's instances, three repetitions, the median, the ten-second budget;
laraserver4 (AMD EPYC 9254, shared, a CI runner busy on it), Temurin 25.0.3 for the JVM, Scala 3.9.0,
Scala Native 0.5.12 with LLVM 18.1.8, Node.js 26.5 for the WebAssembly module. Raw data:
[2026-09-03-laraserver4-graal.tsv](2026-09-03-laraserver4-graal.tsv) (the whole table, princess and
SMTInterpol included).

**Result.** The image starts and answers in 6–30 ms on most instances, where the JVM takes 230–420 ms
and Scala Native 10–170 ms: over the 39 instances answered by both, the JVM is 12.9× slower than the image
(geometric mean) and Scala Native 2.6× slower; the image's median is 11 ms, z3's and cvc5's binaries are
10–20 ms. Where the search itself takes time (`NEQ004`, the wide bit-vectors), the image runs the same
code without the JIT: `NEQ004_size4` 0.36 s against the JVM's 0.61 s and Scala Native's 1.70 s. The
answers agree with the JVM's everywhere.

| Instance | VIK (JVM) | VIK (native) | VIK (GraalVM Native Image) | VIK (Node) | z3 | cvc5 |
|---|---:|---:|---:|---:|---:|---:|
| `QG-classification__qg5__iso_brn001` | 0.33 | 0.05 | 0.01 | 0.10 | 0.02 | 0.01 |
| `QG-classification__qg5__iso_icl662` | 0.42 | 0.17 | 0.03 | 0.15 | 0.02 | 0.02 |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | 0.23 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `20170829-Rodin__smt3534838651727683253` | 0.25 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `20190906-CLEARSY__0016__00003` | 0.26 | 0.02 | 0.01 | 0.09 | 0.02 | 0.01 |
| `TypeSafe__z3.1184131` | 0.25 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `NEQ__NEQ004_size4` | 0.61 | 1.70 | 0.36 | 0.62 | 0.02 | 0.04 |
| `CAV_2009_benchmarks__20-vars__problem__001` | 0.30 | 0.03 | 0.01 | 0.09 | 0.02 | 0.04 |
| `cut_lemmas__10-vars__cut_lemma_01_001` | 0.26 | 0.01 | 0.01 | 0.07 | 0.02 | 0.01 |
| `dillig__20-4` | 0.33 | 0.07 | 0.03 | 0.11 | 0.02 | 0.04 |
| `prime-cone__prime_cone_unsat_3` | 0.26 | 0.01 | 0.01 | 0.07 | 0.02 | 0.01 |
| `convert__convert-tiff2jpg-query-1831` | 0.31 | 0.03 | 0.01 | 0.11 | 0.02 | 0.01 |
| `mathsat__FISCHER1-1-fair` | 0.30 | 0.03 | 0.01 | 0.09 | 0.02 | 0.01 |
| `wisas__xs_8_8` | 0.49 | 0.31 | 0.09 | 0.22 | 0.02 | 0.01 |
| `TwoSquares__smtlib.598294` | 0.27 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `mathsat__Wisa__xs-09-05-2-4-4-5` | 0.52 | 0.36 | 0.10 | 0.23 | 0.02 | 0.02 |
| `keymaera__division_dijkstra-node701` | 0.23 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `Chua__1__VC2__L__Chua-1-VC2-L-chunk-0005` | 0.26 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `RandomDecoupled__pb_real_30_60_45_28` | 0.58 | 0.65 | 0.09 | 0.22 | 0.02 | 0.02 |
| `bench_ab__a649test0047` | 0.43 | 0.71 | 0.07 | 0.19 | 0.02 | 0.01 |
| `sage__app2__bench_512` | 0.49 | 1.27 | 0.17 | 0.32 | 0.08 | 0.05 |
| `pspace__ndist.a.20000` | 0.27 | 0.02 | 0.01 | 0.07 | 🐢 | 0.01 |
| `20172804-Barrett__tests__v1__v1l20086.cvc` | 0.27 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `20172804-Barrett__tests__v10__v10l20080.cvc` | 0.27 | 0.01 | 0.01 | 0.08 | 0.02 | 0.01 |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0` | 0.31 | 0.01 | 0.01 | 0.08 | 0.01 | 0.01 |
| `20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFDTLIA` | 🐢 | 🐢 | 🐢 | 🐢 | 0.04 | 0.11 |
| `alpha-093dddceea58` | **unsat** 0.32 | **unsat** 0.01 | **unsat** 0.01 | **unsat** 0.08 | **unsat** 0.02 | **unsat** 0.01 |
| `alpha-fib_memoized-36bd61c61979` | **sat** 0.34 | **sat** 0.05 | **sat** 0.02 | **sat** 0.11 | **sat** 0.02 | **sat** 0.01 |
| `storeinv__storeinv_t2_np_nf_ai_00001_001.cvc` | 0.27 | 0.01 | 0.01 | 0.07 | 0.01 | 0.01 |
| `swap__swap_invalid_t3_np_nf_ai_00002_009.cvc` | 0.29 | 0.01 | 0.01 | 0.08 | 0.01 | 0.01 |
| `check__array_incompleteness1` | 0.34 | 0.04 | 0.01 | 0.09 | 0.02 | 0.01 |
| `20190906-CLEARSY__0020__00009` | 0.44 | 0.18 | 0.05 | 0.17 | 0.02 | 0.03 |
| `lustre__two_counters.bmc_k100.prefix25` | 0.40 | 0.10 | 0.03 | 0.14 | 0.02 | 0.01 |
| `lustre__ex3.bmc_k100.prefix25` | 1.61 | 3.99 | 1.25 | 1.71 | 0.19 | 0.30 |
| `kind__MESI_i4.ec` | 1.82 | 5.76 | 1.49 | 1.83 | 0.03 | 0.10 |
| `kind__car_6_e2_893.ec` | 4.03 | 🐢 | 5.80 | 6.80 | 0.10 | 0.90 |
| `cpachecker__diskperf_simpl1` | 0.34 | 0.04 | 0.01 | 0.09 | 0.02 | 0.01 |
| `cpachecker__list_false-unreach-call` | 0.88 | 2.33 | 0.62 | 0.81 | 0.03 | 0.04 |
| `2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max` | 0.31 | 0.02 | 0.01 | 0.10 | 0.02 | 0.01 |
| `bmc__stack_k20` | 0.68 | 0.87 | 0.22 | 0.41 | 0.02 | 0.39 |
| `safari__mux` | 0.78 | 1.31 | 0.27 | 0.48 | 0.03 | 0.04 |
| `safari__illinois` | 0.99 | 2.23 | 0.46 | 0.74 | 0.05 | 0.10 |

The two `alpha-` rows read a doubled status before this run: cold.sh took the `:status` of the files' header
comment for a second `check-sat` (fixed in the same commit); the answers are right.
