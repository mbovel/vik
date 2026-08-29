# CDCL(T) solver benchmark results, 2026-08-28, laraserver4, phase saving

VMTF plus phase saving (a decision reuses the polarity the variable last had). Previous run:
[VMTF](2026-08-28-laraserver4-vmtf.md).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 8 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-phase.json](2026-08-28-laraserver4-phase.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.001 | ± 0.000 | 4.5% | 0.001 | 0.001 | 5 × 5 |
| pigeonhole | n=60 | 0.749 | ± 0.021 | 2.8% | 0.637 | 0.813 | 5 × 5 |
| random3Sat | n=30 | 0.331 | ± 0.007 | 2.1% | 0.303 | 0.368 | 5 × 5 |
| random3Sat | n=60 | 2.478 | ± 0.036 | 1.5% | 2.347 | 2.617 | 5 × 5 |
| random5Sat | n=30 | 2.109 | ± 0.030 | 1.4% | 2.026 | 2.321 | 5 × 5 |
| random5Sat | n=60 | 53.975 | ± 1.048 | 1.9% | 49.988 | 59.107 | 5 × 5 |

## Comparison with the previous run

| Benchmark | n | VMTF (ms/op) | VMTF + phase saving (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| pigeonhole | 30 | 0.001 ± 0.000 | 0.001 ± 0.000 | 0.86× |
| pigeonhole | 60 | 0.716 ± 0.016 | 0.749 ± 0.021 | 0.96× |
| random3Sat | 30 | 0.318 ± 0.007 | 0.331 ± 0.007 | 0.96× |
| random3Sat | 60 | 2.418 ± 0.033 | 2.478 ± 0.036 | 0.98× |
| random5Sat | 30 | 2.354 ± 0.040 | 2.109 ± 0.030 | 1.12× |
| random5Sat | 60 | 70.912 ± 0.870 | 53.975 ± 1.048 | 1.31× |

1.12× and 1.31× on `random5Sat`, within noise elsewhere. Against the [2WL run](2026-08-28-laraserver4-2wl.md), VMTF
with phase saving gives 1.51× on `random3Sat` n=60 and 1.10–1.20× on `random5Sat`, but 0.32× on `pigeonhole` n=60.

## Noise and warmup

- CIs ≤ 3%.
