# CDCL(T) solver benchmark results, 2026-08-28, laraserver4, two watched literals

Unit propagation with two watched literals instead of occurrence lists. Code: the `Solver` of the commit that adds
this file. Previous run: [5-SAT](2026-08-28-laraserver4-5sat.md).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 8 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-2wl.json](2026-08-28-laraserver4-2wl.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.001 | ± 0.000 | 5.0% | 0.001 | 0.001 | 5 × 5 |
| pigeonhole | n=60 | 0.239 | ± 0.005 | 2.2% | 0.216 | 0.260 | 5 × 5 |
| random3Sat | n=30 | 0.306 | ± 0.008 | 2.5% | 0.267 | 0.339 | 5 × 5 |
| random3Sat | n=60 | 3.740 | ± 0.055 | 1.5% | 3.593 | 3.901 | 5 × 5 |
| random5Sat | n=30 | 2.316 | ± 0.034 | 1.5% | 2.211 | 2.454 | 5 × 5 |
| random5Sat | n=60 | 65.031 | ± 1.301 | 2.0% | 60.847 | 74.427 | 5 × 5 |

## Comparison with the previous run

| Benchmark | n | Occurrence lists (ms/op) | Two watched literals (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| pigeonhole | 30 | 0.001 ± 0.000 | 0.001 ± 0.000 | 1.08× |
| pigeonhole | 60 | 0.496 ± 0.009 | 0.239 ± 0.005 | 2.07× |
| random3Sat | 30 | 0.299 ± 0.007 | 0.306 ± 0.008 | 0.98× |
| random3Sat | 60 | 6.302 ± 0.069 | 3.740 ± 0.055 | 1.69× |
| random5Sat | 30 | 4.235 ± 0.069 | 2.316 ± 0.034 | 1.83× |
| random5Sat | 60 | 301.057 ± 3.134 | 65.031 ± 1.301 | 4.63× |

The gain grows with clause length and instance size, as expected: 4.6× on `random5Sat` n=60, 1.7–2.1× on the
60-variable 3-SAT and pigeonhole instances, and no change on the two trivial rows.

## Noise and warmup

- CIs ≤ 3%.
