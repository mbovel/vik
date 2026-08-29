# LIA solver benchmark results, 2026-08-29, laraserver4

First full run of `SolverBench`: simplex with branch and bound, `Long` fast-path rationals, before bound propagation.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 3 forks, 5 warmup + 5 measurement iterations of 1 s, average time per operation, one thread.
- 95% confidence interval from the 15 measurement samples (3 forks × 5 iterations), Student's t. Raw data:
  [2026-08-29-laraserver4.json](2026-08-29-laraserver4.json).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=10 | 1.820 | ± 0.021 | 1.1% | 1.789 | 1.921 | 3 × 5 |
| backtrack | n=20 | 77.873 | ± 1.082 | 1.4% | 75.671 | 82.306 | 3 × 5 |
| incremental | n=10 | 2.343 | ± 0.063 | 2.7% | 2.238 | 2.614 | 3 × 5 |
| incremental | n=20 | 40.789 | ± 0.771 | 1.9% | 39.437 | 43.111 | 3 × 5 |
| solve | n=10 | 0.928 | ± 0.021 | 2.2% | 0.889 | 0.987 | 3 × 5 |
| solve | n=20 | 9.619 | ± 0.144 | 1.5% | 9.293 | 10.107 | 3 × 5 |
