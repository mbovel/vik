# CDCL(T) solver benchmark results, 2026-08-28, laraserver4, Luby restarts

VMTF, phase saving and restarts after `luby(i) * 100` conflicts (learned clauses kept). Previous run:
[phase saving](2026-08-28-laraserver4-phase.md). Code: the `Solver` of the commit that adds this file.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 8 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-restarts.json](2026-08-28-laraserver4-restarts.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.001 | ± 0.000 | 3.6% | 0.001 | 0.001 | 5 × 5 |
| pigeonhole | n=60 | 1.673 | ± 0.031 | 1.8% | 1.536 | 1.811 | 5 × 5 |
| random3Sat | n=30 | 0.318 | ± 0.005 | 1.7% | 0.299 | 0.336 | 5 × 5 |
| random3Sat | n=60 | 2.344 | ± 0.032 | 1.4% | 2.228 | 2.457 | 5 × 5 |
| random5Sat | n=30 | 2.146 | ± 0.035 | 1.6% | 2.047 | 2.305 | 5 × 5 |
| random5Sat | n=60 | 23.374 | ± 0.404 | 1.7% | 21.604 | 25.372 | 5 × 5 |

## Comparison with the previous run

| Benchmark | n | VMTF + phase (ms/op) | + Luby restarts (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| pigeonhole | 30 | 0.001 ± 0.000 | 0.001 ± 0.000 | 1.02× |
| pigeonhole | 60 | 0.749 ± 0.021 | 1.673 ± 0.031 | 0.45× |
| random3Sat | 30 | 0.331 ± 0.007 | 0.318 ± 0.005 | 1.04× |
| random3Sat | 60 | 2.478 ± 0.036 | 2.344 ± 0.032 | 1.06× |
| random5Sat | 30 | 2.109 ± 0.030 | 2.146 ± 0.035 | 0.98× |
| random5Sat | 60 | 53.975 ± 1.048 | 23.374 ± 0.404 | 2.31× |

## The package against the two-watched-literals baseline

| Benchmark | n | 2WL, static order (ms/op) | VMTF + phase saving + restarts (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| pigeonhole | 30 | 0.001 | 0.001 | 0.91× |
| pigeonhole | 60 | 0.239 | 1.673 | 0.14× |
| random3Sat | 30 | 0.306 | 0.318 | 0.96× |
| random3Sat | 60 | 3.740 | 2.344 | 1.60× |
| random5Sat | 30 | 2.316 | 2.146 | 1.08× |
| random5Sat | 60 | 65.031 | 23.374 | 2.78× |

Restarts alone give 2.3× on `random5Sat` n=60. The package is kept: it is the standard configuration and wins on the
unstructured workloads; `pigeonhole` is the known adversarial case, where the lexicographic static order happens to
suit the resolution refutation and every dynamic choice loses it. To be judged again on SMT-LIB instances.

## Noise and warmup

- CIs ≤ 3%.
