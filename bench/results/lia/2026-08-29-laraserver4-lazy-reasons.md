# LIA solver benchmark results, lazy bound reasons, 2026-08-30, laraserver4

A derived bound keeps the bounds it was derived from (its premises) instead of the union of their reason sets; the literals are collected only when a conflict needs them (`Bound.reasons`). Kept.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8; 3 forks, 5 warmup + 5 measurement iterations of 1 s, average time per operation (ms), 95% confidence intervals.
- Raw data: [2026-08-29-laraserver4-lazy-reasons.json](2026-08-29-laraserver4-lazy-reasons.json); before: [2026-08-29-laraserver4-propagation.json](2026-08-29-laraserver4-propagation.json) (the previous record, same configuration).

| Benchmark | n | before | after |
|---|---:|---:|---:|
| `backtrack` | 10 | 9.73 ± 0.11 | 4.24 ± 0.16 (2.30×) |
| `backtrack` | 20 | 102.25 ± 1.68 | 41.86 ± 0.90 (2.44×) |
| `incremental` | 10 | 10.41 ± 0.21 | 3.61 ± 0.05 (2.88×) |
| `incremental` | 20 | 164.96 ± 4.21 | 51.44 ± 1.01 (3.21×) |
| `solve` | 10 | 1.58 ± 0.03 | 0.95 ± 0.02 (1.67×) |
| `solve` | 20 | 4.09 ± 0.07 | 3.49 ± 0.06 (1.17×) |
| `stepChains` | 10 | 0.81 ± 0.02 | 0.40 ± 0.01 (2.06×) |
| `stepChains` | 20 | 3.85 ± 0.24 | 1.86 ± 0.05 (2.07×) |
