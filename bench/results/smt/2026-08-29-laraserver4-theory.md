# Theory propagation experiments, 2026-08-29, laraserver4

`OtherSolversBench.vik` (in-process, after warm-up) for four ways of finding the atoms the UF theory implies,
quick configuration: 3 forks, 5 warmup and 5 measurement iterations of 1 s, ms/op with the 95% confidence
interval over the 15 measurements, and the ratio to the first column.

- **scan**: the theory scans all atoms at every `propagate` (`ccd3564`).
- **events**: the e-graph logs merges and asserted disequalities; the theory keeps per-class atom lists and checks the
  atoms of the absorbed class at a merge and of the smaller class at a disequality. Incomplete: an atom of the
  surviving class falsified by a disequality of the absorbed class is not implied, and the search changes (icl662:
  54 decisions and 42 conflicts instead of 34 and 31).
- **complete**: the e-graph also logs the disequalities a merge derives, the theory checks the absorbed class's atoms
  for both equal and disequal sides and, per new disequal pair, the atoms between the two classes; the implied
  literals are returned in variable order. The implications are byte for byte those of the scan (same decisions,
  conflicts and trail on icl662).
- **dirty flag**: the scan, skipped when no atom was asserted since the last `propagate`.

| instance | scan | events | complete | dirty flag |
|---|---:|---:|---:|---:|
| 20170829-Rodin__smt3534838651727683253 | 0.004 ± 0.000 | 0.005 ± 0.000 (0.75×) | 0.004 ± 0.000 (0.96×) | 0.004 ± 0.000 (0.99×) |
| 2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max | 0.004 ± 0.000 | 0.004 ± 0.000 (0.92×) | 0.004 ± 0.001 (0.80×) | 0.004 ± 0.000 (0.95×) |
| 2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max | 2.886 ± 0.061 | 2.771 ± 0.169 (1.04×) | 2.691 ± 0.175 (1.07×) | 2.853 ± 0.070 (1.01×) |
| 20190906-CLEARSY__0016__00003 | 0.003 ± 0.000 | 0.003 ± 0.000 (0.88×) | 0.003 ± 0.000 (0.86×) | 0.003 ± 0.000 (0.94×) |
| NEQ__NEQ004_size4 | 97.526 ± 1.603 | 62.464 ± 1.375 (1.56×) | 52.379 ± 4.103 (1.86×) | 174.277 ± 20.422 (0.56×) |
| PEQ__PEQ018_size4 | 146.383 ± 5.113 | 130.968 ± 1.560 (1.12×) | 154.381 ± 10.583 (0.95×) | 154.754 ± 7.284 (0.95×) |
| QG-classification__qg5__iso_brn001 | 1.272 ± 0.021 | 1.373 ± 0.051 (0.93×) | 1.349 ± 0.031 (0.94×) | 1.386 ± 0.080 (0.92×) |
| QG-classification__qg5__iso_icl662 | 5.651 ± 0.116 | 7.102 ± 0.179 (0.80×) | 6.505 ± 0.166 (0.87×) | 5.739 ± 0.108 (0.98×) |
| SEQ__SEQ032_size3 | 28.743 ± 2.034 | 30.538 ± 3.202 (0.94×) | 27.652 ± 0.583 (1.04×) | 31.622 ± 3.895 (0.91×) |
| TypeSafe__z3.1184131 | 0.005 ± 0.000 | 0.005 ± 0.000 (0.93×) | 0.005 ± 0.000 (0.96×) | 0.005 ± 0.000 (1.03×) |

NEQ004 is bimodal across forks (JIT): re-measured back to back, the scan gave 99–139 ms and the dirty flag
104–160 ms, so the dirty flag changes nothing measurable and is dropped. The complete event-driven theory is
kept: it does the same search with the cost of propagation proportional to the merges and disequalities rather
than to the number of atoms, 1.8× on NEQ004 (all forks near 52 ms), within noise elsewhere except the two
QG-classification instances (−6% and −13%: many merges of classes with many disequalities, few atoms, where
the derived disequality events cost more than a scan of 379 atoms).
