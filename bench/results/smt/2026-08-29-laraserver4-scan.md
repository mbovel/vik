# Back to the scan for theory propagation, 2026-08-29, laraserver4

`OtherSolversBench.vik` (3 forks, 5 warmup and 5 measurement iterations of 1 s), ms/op with the 95% confidence
interval: the event-driven propagation of `6badd0f` (kept for a day) against the scan of the unassigned atoms at every
propagate, restored for readability (60 lines of linked atom lists and marks in the theory and an event log with
derived disequalities in the e-graph, for 1.8× on NEQ004 and nothing elsewhere). Raw JMH JSON:
`2026-08-29-laraserver4-scan.json` (the "events" column is the record of the morning plus the QF_LIA baseline).

| instance | events | scan |
|---|---:|---:|
| 20170829-Rodin__smt3534838651727683253 | 0.004 ± 0.000 | 0.004 ± 0.000 (1.12×) |
| 2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max | 0.004 ± 0.000 | 0.004 ± 0.000 (1.04×) |
| 2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max | 2.507 ± 0.184 | 2.718 ± 0.037 (0.92×) |
| 20190906-CLEARSY__0016__00003 | 0.003 ± 0.001 | 0.003 ± 0.000 (1.13×) |
| CAV_2009_benchmarks__20-vars__problem__001 | 2.372 ± 0.050 | 2.460 ± 0.107 (0.96×) |
| CIRC__multiplier_prime__MULTIPLIER_PRIME_2.msat | 0.154 ± 0.003 | 0.159 ± 0.007 (0.97×) |
| NEQ__NEQ004_size4 | 40.217 ± 1.046 | 94.021 ± 2.760 (0.43×) |
| PEQ__PEQ018_size4 | 120.365 ± 2.705 | 123.788 ± 2.387 (0.97×) |
| QG-classification__qg5__iso_brn001 | 0.716 ± 0.062 | 0.677 ± 0.011 (1.06×) |
| QG-classification__qg5__iso_icl662 | 4.517 ± 0.099 | 3.703 ± 0.061 (1.22×) |
| SEQ__SEQ032_size3 | 11.293 ± 0.222 | 10.833 ± 0.367 (1.04×) |
| TypeSafe__z3.1184131 | 0.004 ± 0.000 | 0.003 ± 0.000 (1.06×) |
| calypto__problem-005675.cvc.2 | 2.116 ± 0.115 | 2.120 ± 0.083 (1.00×) |
| convert__convert-tiff2jpg-query-1831 | 0.417 ± 0.027 | 0.396 ± 0.022 (1.05×) |
| cut_lemmas__10-vars__cut_lemma_01_001 | 0.009 ± 0.000 | 0.009 ± 0.000 (1.00×) |
| dillig__20-4 | 10.209 ± 0.332 | 9.925 ± 0.144 (1.03×) |
| mathsat__FISCHER1-1-fair | 0.636 ± 0.014 | 0.628 ± 0.011 (1.01×) |
| pidgeons__pigeon-hole-6 | 0.334 ± 0.022 | 0.319 ± 0.013 (1.05×) |
| prime-cone__prime_cone_unsat_3 | 0.056 ± 0.000 | 0.057 ± 0.001 (0.99×) |
| rings__ring_2exp4_3vars_2ite_unsat | 54.282 ± 3.100 | 52.442 ± 1.894 (1.04×) |
