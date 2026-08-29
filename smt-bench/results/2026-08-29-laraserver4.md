# SMT-LIB benchmark results, 2026-08-29, laraserver4

VIK (`SmtBench.solve`, `OtherSolversBench.vik`) on the ten QF_UF instances it solves in seconds, the parser and SMTInterpol on five of them, princess on four; four JMH runs merged, all after the event-driven theory, the cached hashes and the linear LCA.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 3 forks, 5 warmup + 5 measurement iterations of 1 s, average time per operation, one thread.
- 95% confidence interval from the 15 measurement samples (3 forks × 5 iterations), Student's t. Raw data:
  [2026-08-29-laraserver4.json](2026-08-29-laraserver4.json).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| vik | file=NEQ__NEQ004_size4.smt2 | 40.217 | ± 1.046 | 2.6% | 38.280 | 44.249 | 3 × 5 |
| vik | file=PEQ__PEQ018_size4.smt2 | 120.365 | ± 2.705 | 2.2% | 115.977 | 131.112 | 3 × 5 |
| vik | file=SEQ__SEQ032_size3.smt2 | 11.293 | ± 0.222 | 2.0% | 10.880 | 11.887 | 3 × 5 |
| vik | file=QG-classification__qg5__iso_brn001.smt2 | 0.716 | ± 0.062 | 8.6% | 0.643 | 1.048 | 3 × 5 |
| vik | file=QG-classification__qg5__iso_icl662.smt2 | 4.517 | ± 0.099 | 2.2% | 4.316 | 4.779 | 3 × 5 |
| vik | file=2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2 | 0.004 | ± 0.000 | 3.1% | 0.004 | 0.005 | 3 × 5 |
| vik | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 2.507 | ± 0.184 | 7.3% | 2.155 | 3.191 | 3 × 5 |
| vik | file=20170829-Rodin__smt3534838651727683253.smt2 | 0.004 | ± 0.000 | 7.1% | 0.004 | 0.005 | 3 × 5 |
| vik | file=20190906-CLEARSY__0016__00003.smt2 | 0.003 | ± 0.001 | 16.5% | 0.003 | 0.006 | 3 × 5 |
| vik | file=TypeSafe__z3.1184131.smt2 | 0.004 | ± 0.000 | 5.8% | 0.003 | 0.004 | 3 × 5 |
| solve | file=NEQ__NEQ004_size4.smt2 | 41.285 | ± 1.447 | 3.5% | 38.182 | 46.117 | 3 × 5 |
| solve | file=PEQ__PEQ018_size4.smt2 | 122.575 | ± 2.292 | 1.9% | 117.500 | 127.752 | 3 × 5 |
| solve | file=SEQ__SEQ032_size3.smt2 | 11.625 | ± 0.361 | 3.1% | 10.880 | 12.777 | 3 × 5 |
| solve | file=QG-classification__qg5__iso_brn001.smt2 | 0.694 | ± 0.015 | 2.2% | 0.661 | 0.760 | 3 × 5 |
| solve | file=QG-classification__qg5__iso_icl662.smt2 | 4.401 | ± 0.058 | 1.3% | 4.277 | 4.662 | 3 × 5 |
| solve | file=2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2 | 0.004 | ± 0.000 | 2.4% | 0.004 | 0.004 | 3 × 5 |
| solve | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 2.563 | ± 0.187 | 7.3% | 2.211 | 3.296 | 3 × 5 |
| solve | file=20170829-Rodin__smt3534838651727683253.smt2 | 0.004 | ± 0.000 | 6.4% | 0.004 | 0.005 | 3 × 5 |
| solve | file=20190906-CLEARSY__0016__00003.smt2 | 0.003 | ± 0.000 | 5.4% | 0.003 | 0.003 | 3 × 5 |
| solve | file=TypeSafe__z3.1184131.smt2 | 0.004 | ± 0.000 | 4.3% | 0.003 | 0.004 | 3 × 5 |
| parse | file=NEQ__NEQ004_size4.smt2 | 0.592 | ± 0.016 | 2.6% | 0.559 | 0.657 | 3 × 5 |
| parse | file=SEQ__SEQ032_size3.smt2 | 0.128 | ± 0.005 | 3.6% | 0.122 | 0.149 | 3 × 5 |
| parse | file=QG-classification__qg5__iso_icl662.smt2 | 1.714 | ± 0.029 | 1.7% | 1.665 | 1.817 | 3 × 5 |
| parse | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 2.963 | ± 0.076 | 2.6% | 2.832 | 3.351 | 3 × 5 |
| parse | file=20190906-CLEARSY__0016__00003.smt2 | 0.145 | ± 0.003 | 1.8% | 0.141 | 0.154 | 3 × 5 |
| princess | file=QG-classification__qg5__iso_icl662.smt2 | 290.438 | ± 7.822 | 2.7% | 274.584 | 320.065 | 3 × 5 |
| princess | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 26298.613 | ± 1578.022 | 6.0% | 23721.280 | 31571.342 | 3 × 5 |
| princess | file=20190906-CLEARSY__0016__00003.smt2 | 19.657 | ± 0.711 | 3.6% | 18.097 | 22.140 | 3 × 5 |
| princess | file=TypeSafe__z3.1184131.smt2 | 1.933 | ± 0.063 | 3.3% | 1.796 | 2.175 | 3 × 5 |
| smtinterpol | file=NEQ__NEQ004_size4.smt2 | 117.696 | ± 3.363 | 2.9% | 112.505 | 129.467 | 3 × 5 |
| smtinterpol | file=SEQ__SEQ032_size3.smt2 | 11.473 | ± 0.405 | 3.5% | 10.710 | 12.934 | 3 × 5 |
| smtinterpol | file=QG-classification__qg5__iso_icl662.smt2 | 17.999 | ± 0.277 | 1.5% | 17.476 | 19.011 | 3 × 5 |
| smtinterpol | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 31.419 | ± 0.969 | 3.1% | 29.400 | 35.019 | 3 × 5 |
| smtinterpol | file=20190906-CLEARSY__0016__00003.smt2 | 1.154 | ± 0.061 | 5.3% | 1.041 | 1.345 | 3 × 5 |
