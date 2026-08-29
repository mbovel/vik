# Linear lowest common ancestor in explanations, 2026-08-29, laraserver4

`OtherSolversBench.vik` (in-process, after warm-up), quick configuration: 3 forks, 5 warmup and 5 measurement
iterations of 1 s, ms/op with the 95% confidence interval over the 15 measurements, and the ratio to the previous
step. Raw JMH JSON next to this file.

`EGraph.explain` found the lowest common ancestor of the two sides in the union tree with `pathX.find(pathY.contains)`, quadratic in the path lengths with boxed comparisons, and allocated both paths; it now marks the ancestors of one side with a stamp and climbs from the other, then walks both paths once for the most recent edge, without allocation. Kept: SEQ032 2.2×, NEQ004 1.3×, PEQ018 1.2×, the rest within noise.

| instance | cached hashes | linear LCA |
|---|---:|---:|
| 20170829-Rodin__smt3534838651727683253 | 0.004 ± 0.000 | 0.004 ± 0.000 (0.99×) |
| 2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max | 0.004 ± 0.000 | 0.004 ± 0.000 (0.99×) |
| 2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max | 2.477 ± 0.158 | 2.588 ± 0.217 (0.96×) |
| 20190906-CLEARSY__0016__00003 | 0.003 ± 0.000 | 0.003 ± 0.000 (1.00×) |
| NEQ__NEQ004_size4 | 53.803 ± 5.120 | 40.357 ± 0.841 (1.33×) |
| PEQ__PEQ018_size4 | 159.914 ± 19.780 | 132.182 ± 12.229 (1.21×) |
| QG-classification__qg5__iso_brn001 | 0.722 ± 0.044 | 0.700 ± 0.023 (1.03×) |
| QG-classification__qg5__iso_icl662 | 4.993 ± 0.122 | 4.621 ± 0.213 (1.08×) |
| SEQ__SEQ032_size3 | 26.640 ± 0.662 | 11.847 ± 0.274 (2.25×) |
| TypeSafe__z3.1184131 | 0.004 ± 0.000 | 0.004 ± 0.000 (1.24×) |
