# Cached structural hashes, 2026-08-29, laraserver4

`OtherSolversBench.vik` (in-process, after warm-up), quick configuration: 3 forks, 5 warmup and 5 measurement
iterations of 1 s, ms/op with the 95% confidence interval over the 15 measurements, and the ratio to the previous
step. Raw JMH JSON next to this file.

Term and Formula compute their structural hash once (a lazy val): the encoder looks every subterm and subformula up in hash maps, and the QG-classification instances share large subterms through `let`, so the case-class hash, recomputed over the whole tree at each lookup, was a tenth of the profile. Kept: brn001 1.9×, icl662 1.3×, mcs 1.1×, the rest within noise.

| instance | theory events | cached hashes |
|---|---:|---:|
| 20170829-Rodin__smt3534838651727683253 | 0.004 ± 0.000 | 0.004 ± 0.000 (1.02×) |
| 2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max | 0.004 ± 0.001 | 0.004 ± 0.000 (1.12×) |
| 2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max | 2.691 ± 0.175 | 2.477 ± 0.158 (1.09×) |
| 20190906-CLEARSY__0016__00003 | 0.003 ± 0.000 | 0.003 ± 0.000 (1.18×) |
| NEQ__NEQ004_size4 | 52.379 ± 4.103 | 53.803 ± 5.120 (0.97×) |
| PEQ__PEQ018_size4 | 154.381 ± 10.583 | 159.914 ± 19.780 (0.97×) |
| QG-classification__qg5__iso_brn001 | 1.349 ± 0.031 | 0.722 ± 0.044 (1.87×) |
| QG-classification__qg5__iso_icl662 | 6.505 ± 0.166 | 4.993 ± 0.122 (1.30×) |
| SEQ__SEQ032_size3 | 27.652 ± 0.583 | 26.640 ± 0.662 (1.04×) |
| TypeSafe__z3.1184131 | 0.005 ± 0.000 | 0.004 ± 0.000 (1.13×) |
