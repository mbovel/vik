# SMT benchmark results, higher-order core, 2026-08-30, laraserver4

VIK in process after the higher-order core (curried applications, extensionality witnesses, `Bool` as a sort): `SmtBench.solve` on QF_UF, QF_DT and QF_UFLIA instances, `LambdaBench`, `UfliaBench`. Before: the previous record of each benchmark ([2026-08-29-laraserver4](2026-08-29-laraserver4.json) for QF_UF, [dt-instances](2026-08-29-laraserver4-dt-instances.json) for QF_DT, [uflia-instances](2026-08-29-laraserver4-uflia-instances.json) and [uflia](2026-08-29-laraserver4-uflia.json) for the rest).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8; 3 forks, 5 warmup + 5 measurement iterations of 1 s, average time per operation (ms), 95% confidence intervals. Raw data: [2026-08-30-laraserver4-hocore.json](2026-08-30-laraserver4-hocore.json).

| Benchmark | Params | Before (ms/op) | After (ms/op) | Ratio |
|---|---|---:|---:|---:|
| `LambdaBench.sat` | n=10 | 0.069 ± 0.005 | 0.084 ± 0.005 | 0.81× |
| `LambdaBench.sat` | n=30 | 0.227 ± 0.005 | 0.290 ± 0.014 | 0.78× |
| `LambdaBench.unsat` | n=10 | 0.068 ± 0.003 | 0.087 ± 0.005 | 0.78× |
| `LambdaBench.unsat` | n=30 | 0.231 ± 0.005 | 0.296 ± 0.019 | 0.78× |
| `SmtBench.solve` | file=20172804-Barrett__tests__v10__v10l20080.cvc.smt2 | 0.011 ± 0.001 | 0.014 ± 0.001 | 0.79× |
| `SmtBench.solve` | file=20172804-Barrett__tests__v10__v10l60049.cvc.smt2 | 0.023 ± 0.001 | 0.027 ± 0.001 | 0.83× |
| `SmtBench.solve` | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 2.563 ± 0.187 | 2.933 ± 0.131 | 0.87× |
| `SmtBench.solve` | file=20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2 | 0.125 ± 0.007 | 0.182 ± 0.010 | 0.69× |
| `SmtBench.solve` | file=20230720-blocksworld__from_11_0_0_to_6_4_1_bmc_1.smt2 | 0.453 ± 0.031 | 0.580 ± 0.021 | 0.78× |
| `SmtBench.solve` | file=NEQ__NEQ004_size4.smt2 | 41.285 ± 1.447 | 92.430 ± 2.774 | 0.45× |
| `SmtBench.solve` | file=PEQ__PEQ018_size4.smt2 | 122.575 ± 2.292 | 125.274 ± 2.452 | 0.98× |
| `SmtBench.solve` | file=QG-classification__qg5__iso_brn001.smt2 | 0.694 ± 0.015 | 0.665 ± 0.013 | 1.04× |
| `SmtBench.solve` | file=QG-classification__qg5__iso_icl662.smt2 | 4.401 ± 0.058 | 3.902 ± 0.153 | 1.13× |
| `SmtBench.solve` | file=SEQ__SEQ032_size3.smt2 | 11.625 ± 0.361 | 10.881 ± 0.271 | 1.07× |
| `SmtBench.solve` | file=wisas__xs_20_20.smt2 | 726.302 ± 8.032 | 744.113 ± 7.736 | 0.98× |
| `UfliaBench.solve` | n=10 | 200.036 ± 10.220 | 188.600 ± 3.261 | 1.06× |
| `UfliaBench.solve` | n=5 | 1.703 ± 0.072 | 1.710 ± 0.064 | 1.00× |

Notes:
- The higher-order core itself is neutral: at the previous commit (`966a038`) the same instances measured NEQ004 95 ms, blocksworld `bmc_0` 0.216 ms, icl662 5.1 ms (3 or 2 forks, quick configuration). The overhead it first added — sort strings rebuilt and re-parsed for every term, a walk of every application in the lemma scan — was removed by memoizing the sorts and skipping the passes over all terms when no integer, function term or function equality exists (`Encoder.anyInt`, `anyFunction`, `anyFunctionEquality`).
- NEQ004 at 92 ms against 41 ms: the 41 ms record was the event-driven theory propagation, reverted for readability on 2026-08-29 ([scan](2026-08-29-laraserver4-scan.md)); NEQ004 has measured 92–98 ms since.
- Blocksworld `bmc_0` 0.139 ms at `bdd059a` and `d07c461`, 0.215 ms at `e9f90ad` (the sharing rework): part of it was the passes above, now skipped (0.182 ms); the rest is not located.
- LambdaBench 0.78×: the extensionality scan and the generalized application lemmas (partial applications too) cost about 0.06 ms per solve here.
- Two-valuedness (merging a false Boolean with the `false` class) and the atoms for Boolean terms apply to Booleans in argument position or compared by `=` only; on the QF_UF and QF_DT instances neither mechanism measured (both switched off: within noise).
