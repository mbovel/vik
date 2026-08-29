# SMT-LIB QF_BV benchmark results, 2026-08-29, laraserver4

VIK on the ten QF_BV instances in the arithmetic fragment lowered to integers (`SmtBench.solve`: parse and solve; `OtherSolversBench.vik`: solve the parsed script), 3 forks.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 3 forks, 5 warmup + 5 measurement iterations of 1 s, average time per operation, one thread.
- 95% confidence interval from the 15 measurement samples (3 forks × 5 iterations), Student's t. Raw data:
  [2026-08-29-laraserver4-bv.json](2026-08-29-laraserver4-bv.json).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| vik | file=sage__app7__bench_8505.smt2 | 0.005 | ± 0.000 | 5.2% | 0.005 | 0.007 | 3 × 5 |
| vik | file=bench_ab__a97test0001.smt2 | 0.004 | ± 0.000 | 3.6% | 0.004 | 0.005 | 3 × 5 |
| vik | file=bmc-bv__adpcm.smt2 | 0.001 | ± 0.000 | 2.4% | 0.001 | 0.001 | 3 × 5 |
| vik | file=bmc-bv__Prim_4.smt2 | 0.001 | ± 0.000 | 3.3% | 0.000 | 0.001 | 3 × 5 |
| vik | file=crafted__bitops7.smt2 | 0.126 | ± 0.002 | 1.8% | 0.122 | 0.137 | 3 × 5 |
| vik | file=crafted__bv8.smt2 | 0.016 | ± 0.001 | 5.0% | 0.015 | 0.021 | 3 × 5 |
| vik | file=2018-Goel-hwbench__QF_BV_bv_bv_swap_two_cc_ref_max.smt2 | 0.282 | ± 0.018 | 6.5% | 0.259 | 0.377 | 3 × 5 |
| vik | file=2018-Goel-hwbench__QF_BV_cav14_example_cc_reg_max.smt2 | 0.491 | ± 0.028 | 5.6% | 0.451 | 0.591 | 3 × 5 |
| vik | file=20190311-bv-term-small-rw-Noetzli__bv-term-small-rw_17.smt2 | 0.004 | ± 0.000 | 6.1% | 0.004 | 0.006 | 3 × 5 |
| vik | file=pspace__ndist.a.20000.smt2 | 0.317 | ± 0.013 | 4.1% | 0.297 | 0.395 | 3 × 5 |
| solve | file=sage__app7__bench_8505.smt2 | 0.005 | ± 0.000 | 6.1% | 0.005 | 0.007 | 3 × 5 |
| solve | file=bench_ab__a97test0001.smt2 | 0.004 | ± 0.000 | 3.7% | 0.004 | 0.005 | 3 × 5 |
| solve | file=bmc-bv__adpcm.smt2 | 0.001 | ± 0.000 | 1.5% | 0.001 | 0.001 | 3 × 5 |
| solve | file=bmc-bv__Prim_4.smt2 | 0.001 | ± 0.000 | 1.9% | 0.000 | 0.001 | 3 × 5 |
| solve | file=crafted__bitops7.smt2 | 0.128 | ± 0.005 | 3.9% | 0.124 | 0.160 | 3 × 5 |
| solve | file=crafted__bv8.smt2 | 0.016 | ± 0.001 | 3.6% | 0.015 | 0.020 | 3 × 5 |
| solve | file=2018-Goel-hwbench__QF_BV_bv_bv_swap_two_cc_ref_max.smt2 | 0.274 | ± 0.009 | 3.4% | 0.259 | 0.317 | 3 × 5 |
| solve | file=2018-Goel-hwbench__QF_BV_cav14_example_cc_reg_max.smt2 | 0.488 | ± 0.027 | 5.4% | 0.452 | 0.580 | 3 × 5 |
| solve | file=20190311-bv-term-small-rw-Noetzli__bv-term-small-rw_17.smt2 | 0.004 | ± 0.000 | 4.3% | 0.004 | 0.005 | 3 × 5 |
| solve | file=pspace__ndist.a.20000.smt2 | 0.321 | ± 0.015 | 4.6% | 0.289 | 0.391 | 3 × 5 |
