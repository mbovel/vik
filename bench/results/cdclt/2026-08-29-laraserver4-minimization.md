# Learned clause minimization, 2026-08-29, laraserver4

Experiment, not kept: after conflict analysis, a literal of the learned clause whose reason clause consists,
recursively, of literals of the learned clause is dropped (Sörensson and Biere, "Minimizing learned clauses", SAT 2009,
as MiniSat does), about 40 lines. Quick configuration (3 forks, 5 warmup and 5 measurement iterations of 1 s), ms/op with
the 95% confidence interval; ratios against the solver without minimization. Raw JMH JSON:
`2026-08-29-laraserver4-minimization-{base,min}.json`.

| Benchmark | n | without | with minimization |
|---|---:|---:|---:|
| `pigeonhole` | 30 | 0.00 ± 0.00 | 0.00 ± 0.00 (0.74×) |
| `pigeonhole` | 60 | 1.63 ± 0.05 | 2.07 ± 0.18 (0.79×) |
| `random3Sat` | 30 | 0.32 ± 0.01 | 0.34 ± 0.01 (0.94×) |
| `random3Sat` | 60 | 2.29 ± 0.05 | 2.49 ± 0.04 (0.92×) |
| `random5Sat` | 30 | 2.09 ± 0.03 | 2.15 ± 0.04 (0.97×) |
| `random5Sat` | 60 | 22.06 ± 0.20 | 18.15 ± 0.22 (1.22×) |

VIK in process on the SMT-LIB instances of smt-bench (`OtherSolversBench.vik`):

| instance | without | with minimization |
|---|---:|---:|
| 20170829-Rodin__smt3534838651727683253 | 0.004 ± 0.000 | 0.004 ± 0.000 (1.03×) |
| 2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max | 0.004 ± 0.000 | 0.004 ± 0.000 (1.05×) |
| 2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max | 2.507 ± 0.184 | 2.551 ± 0.121 (0.98×) |
| 20190906-CLEARSY__0016__00003 | 0.003 ± 0.001 | 0.003 ± 0.000 (1.09×) |
| CAV_2009_benchmarks__20-vars__problem__001 | 2.372 ± 0.050 | 2.378 ± 0.049 (1.00×) |
| CIRC__multiplier_prime__MULTIPLIER_PRIME_2.msat | 0.154 ± 0.003 | 0.153 ± 0.003 (1.01×) |
| NEQ__NEQ004_size4 | 40.217 ± 1.046 | 40.893 ± 1.245 (0.98×) |
| PEQ__PEQ018_size4 | 120.365 ± 2.705 | 127.229 ± 2.127 (0.95×) |
| QG-classification__qg5__iso_brn001 | 0.716 ± 0.062 | 0.694 ± 0.027 (1.03×) |
| QG-classification__qg5__iso_icl662 | 4.517 ± 0.099 | 4.486 ± 0.077 (1.01×) |
| SEQ__SEQ032_size3 | 11.293 ± 0.222 | 11.085 ± 0.347 (1.02×) |
| TypeSafe__z3.1184131 | 0.004 ± 0.000 | 0.004 ± 0.000 (1.00×) |
| calypto__problem-005675.cvc.2 | 2.116 ± 0.115 | 2.051 ± 0.112 (1.03×) |
| convert__convert-tiff2jpg-query-1831 | 0.417 ± 0.027 | 0.401 ± 0.022 (1.04×) |
| cut_lemmas__10-vars__cut_lemma_01_001 | 0.009 ± 0.000 | 0.009 ± 0.000 (1.00×) |
| dillig__20-4 | 10.209 ± 0.332 | 9.896 ± 0.138 (1.03×) |
| mathsat__FISCHER1-1-fair | 0.636 ± 0.014 | 0.634 ± 0.019 (1.00×) |
| pidgeons__pigeon-hole-6 | 0.334 ± 0.022 | 0.322 ± 0.009 (1.04×) |
| prime-cone__prime_cone_unsat_3 | 0.056 ± 0.000 | 0.057 ± 0.001 (0.99×) |
| rings__ring_2exp4_3vars_2ite_unsat | 54.282 ± 3.100 | 52.031 ± 1.837 (1.04×) |

Within noise on every SMT-LIB instance and on random 3-SAT, 1.2× on random 5-SAT at n = 60, 0.75× on pigeonhole:
not worth the code under the rule that readability weighs more than a gain on one workload.
