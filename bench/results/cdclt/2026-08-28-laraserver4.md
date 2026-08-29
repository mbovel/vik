# CDCL(T) solver benchmark results, 2026-08-28, laraserver4

Baseline for the unoptimized `Solver` (occurrence-list propagation, static decision order, first-UIP learning,
no restarts or deletion), commit `656a809`.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 5 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4.json](2026-08-28-laraserver4.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.002 | ± 0.000 | 2.2% | 0.002 | 0.002 | 5 × 5 |
| pigeonhole | n=60 | 2.903 | ± 0.041 | 1.4% | 2.789 | 3.075 | 5 × 5 |
| random3Sat | n=30 | 1.599 | ± 0.023 | 1.4% | 1.526 | 1.693 | 5 × 5 |
| random3Sat | n=60 | 15.537 | ± 0.250 | 1.6% | 14.690 | 16.627 | 5 × 5 |

`random3Sat` is ten instances per operation, so 0.16 and 1.6 ms per instance of 30 and 60 variables. `pigeonhole`
n=30 is 3 pigeons, trivial; n=60 is 7 pigeons.

## Noise and warmup

- CIs ≤ 2.2%.
