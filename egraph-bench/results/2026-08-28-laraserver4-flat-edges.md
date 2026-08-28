# E-graph benchmark results, 2026-08-28, laraserver4, flat edges

Experiment: the union tree stored as parallel arrays (`edgeInto`, `edgeA`, `edgeB`, `edgeReason`, `edgeN1`, `edgeN2`,
`edgeTime`) instead of one `Edge` object (with a `Justification`) per merge, `Edge` becoming a view built only by
`explain`. Not adopted: see the comparison below. Code: the [while-loops commit](2026-08-28-laraserver4-while.md)
with that change.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 15 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-flat-edges.json](2026-08-28-laraserver4-flat-edges.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.295 | ± 0.002 | 0.7% | 0.287 | 0.308 | 5 × 5 |
| backtrack | n=4000 | 1.507 | ± 0.021 | 1.4% | 1.454 | 1.630 | 5 × 5 |
| build | n=1000 | 0.197 | ± 0.002 | 1.0% | 0.191 | 0.211 | 5 × 5 |
| build | n=4000 | 0.970 | ± 0.007 | 0.7% | 0.948 | 1.026 | 5 × 5 |
| chain | n=1000 | 0.122 | ± 0.001 | 0.6% | 0.120 | 0.127 | 5 × 5 |
| chain | n=4000 | 0.482 | ± 0.004 | 0.7% | 0.473 | 0.507 | 5 × 5 |
| explain | n=1000 | 0.264 | ± 0.009 | 3.2% | 0.246 | 0.347 | 5 × 5 |
| explain | n=4000 | 1.386 | ± 0.013 | 0.9% | 1.332 | 1.431 | 5 × 5 |
| randomBatch | n=1000 | 0.244 | ± 0.003 | 1.3% | 0.236 | 0.270 | 5 × 5 |
| randomBatch | n=4000 | 1.340 | ± 0.034 | 2.5% | 1.263 | 1.620 | 5 × 5 |
| star | n=1000 | 0.124 | ± 0.002 | 1.3% | 0.118 | 0.129 | 5 × 5 |
| star | n=4000 | 0.482 | ± 0.005 | 1.1% | 0.453 | 0.499 | 5 × 5 |

## Comparison with the previous run (edge objects)

| Benchmark | n | Edge objects (ms/op) | Flat edges (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| backtrack | 1000 | 0.274 ± 0.002 | 0.295 ± 0.002 | 0.93× |
| backtrack | 4000 | 1.439 ± 0.019 | 1.507 ± 0.021 | 0.95× |
| build | 1000 | 0.177 ± 0.002 | 0.197 ± 0.002 | 0.90× |
| build | 4000 | 0.901 ± 0.004 | 0.970 ± 0.007 | 0.93× |
| chain | 1000 | 0.114 ± 0.001 | 0.122 ± 0.001 | 0.93× |
| chain | 4000 | 0.454 ± 0.005 | 0.482 ± 0.004 | 0.94× |
| explain | 1000 | 0.233 ± 0.002 | 0.264 ± 0.009 | 0.88× |
| explain | 4000 | 1.282 ± 0.007 | 1.386 ± 0.013 | 0.92× |
| randomBatch | 1000 | 0.225 ± 0.005 | 0.244 ± 0.003 | 0.92× |
| randomBatch | 4000 | 1.226 ± 0.015 | 1.340 ± 0.034 | 0.92× |
| star | 1000 | 0.107 ± 0.001 | 0.124 ± 0.002 | 0.86× |
| star | 4000 | 0.423 ± 0.007 | 0.482 ± 0.005 | 0.88× |

Slower on every row (0.86–0.95×, disjoint intervals), including `build`, which only writes one array slot per class,
so part of the shift is probably machine load; but nothing suggests a gain, and seven parallel arrays plus a view are
less clear than one small object per merge. The `Edge` objects are kept.

## Noise and warmup

- CIs ≤ 3.5%; worst last-three-warmup vs measurement deviation 23% (one fork of `explain` n=1000).
