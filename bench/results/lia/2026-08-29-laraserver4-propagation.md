# LIA solver benchmark results, with bound propagation, 2026-08-29, laraserver4

Record on the final code of the day: incremental bound propagation (one row visit per propagation), branch and bound, `Long` fast-path rationals, including the new `stepChains` workload; compared with the record before propagation.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 3 forks, 5 warmup + 5 measurement iterations of 1 s, average time per operation, one thread.
- 95% confidence interval from the 15 measurement samples (3 forks × 5 iterations), Student's t. Raw data:
  [2026-08-29-laraserver4-propagation.json](2026-08-29-laraserver4-propagation.json).

Compared with [2026-08-29-laraserver4](2026-08-29-laraserver4.json):

| Benchmark | Params | Before (ms/op) | After (ms/op) | Before / after |
|---|---|---:|---:|---:|
| `backtrack` | n=10 | 1.820 ± 0.021 | 9.728 ± 0.108 | 0.19× |
| `backtrack` | n=20 | 77.873 ± 1.082 | 102.247 ± 1.681 | 0.76× |
| `incremental` | n=10 | 2.343 ± 0.063 | 10.409 ± 0.209 | 0.23× |
| `incremental` | n=20 | 40.789 ± 0.771 | 164.960 ± 4.212 | 0.25× |
| `solve` | n=10 | 0.928 ± 0.021 | 1.584 ± 0.035 | 0.59× |
| `solve` | n=20 | 9.619 ± 0.144 | 4.088 ± 0.069 | 2.35× |

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=10 | 9.728 | ± 0.108 | 1.1% | 9.546 | 10.130 | 3 × 5 |
| backtrack | n=20 | 102.247 | ± 1.681 | 1.6% | 99.062 | 108.563 | 3 × 5 |
| incremental | n=10 | 10.409 | ± 0.209 | 2.0% | 10.053 | 11.138 | 3 × 5 |
| incremental | n=20 | 164.960 | ± 4.212 | 2.6% | 160.005 | 184.588 | 3 × 5 |
| solve | n=10 | 1.584 | ± 0.035 | 2.2% | 1.517 | 1.700 | 3 × 5 |
| solve | n=20 | 4.088 | ± 0.069 | 1.7% | 3.955 | 4.306 | 3 × 5 |
| stepChains | n=10 | 0.814 | ± 0.017 | 2.1% | 0.783 | 0.865 | 3 × 5 |
| stepChains | n=20 | 3.846 | ± 0.237 | 6.2% | 3.479 | 4.642 | 3 × 5 |
