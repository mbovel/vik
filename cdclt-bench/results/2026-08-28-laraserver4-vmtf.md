# CDCL(T) solver benchmark results, 2026-08-28, laraserver4, VMTF

Variable move-to-front decision heuristic (Biere and Fröhlich 2015, as in CreuSAT) on top of two watched literals,
alone: no phase saving, no restarts. Previous run: [2WL](2026-08-28-laraserver4-2wl.md).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 8 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-vmtf.json](2026-08-28-laraserver4-vmtf.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.001 | ± 0.000 | 1.6% | 0.001 | 0.001 | 5 × 5 |
| pigeonhole | n=60 | 0.716 | ± 0.016 | 2.3% | 0.650 | 0.793 | 5 × 5 |
| random3Sat | n=30 | 0.318 | ± 0.007 | 2.1% | 0.294 | 0.345 | 5 × 5 |
| random3Sat | n=60 | 2.418 | ± 0.033 | 1.4% | 2.306 | 2.531 | 5 × 5 |
| random5Sat | n=30 | 2.354 | ± 0.040 | 1.7% | 2.242 | 2.540 | 5 × 5 |
| random5Sat | n=60 | 70.912 | ± 0.870 | 1.2% | 67.539 | 74.326 | 5 × 5 |

## Comparison with the previous run

| Benchmark | n | Static order (ms/op) | VMTF (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| pigeonhole | 30 | 0.001 ± 0.000 | 0.001 ± 0.000 | 1.04× |
| pigeonhole | 60 | 0.239 ± 0.005 | 0.716 ± 0.016 | 0.33× |
| random3Sat | 30 | 0.306 ± 0.008 | 0.318 ± 0.007 | 0.96× |
| random3Sat | 60 | 3.740 ± 0.055 | 2.418 ± 0.033 | 1.55× |
| random5Sat | 30 | 2.316 ± 0.034 | 2.354 ± 0.040 | 0.98× |
| random5Sat | 60 | 65.031 ± 1.301 | 70.912 ± 0.870 | 0.92× |

Mixed on its own: 1.55× on `random3Sat` n=60, 0.92× on `random5Sat` n=60 and 0.33× on `pigeonhole` n=60, whose
static variable order happens to suit resolution refutations. Judged together with phase saving and restarts (the
following runs).

## Noise and warmup

- CIs ≤ 3%.
