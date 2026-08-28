# E-graph benchmark results, 2026-08-28, laraserver4

Baseline for the current `EGraph` implementation (`LongMap[EClass]` classes, `HashMap[ENode, EClassId]` hashcons,
`repair` rescanning every parent of the merged-into class), before any optimization.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users (load average ≈ 13 at start).
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8. Code: commit `48386fd` (e-graph unchanged since `6a06baf`).
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 13 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4.json](2026-08-28-laraserver4.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| build | n=1000 | 0.436 | ± 0.021 | 4.8% | 0.374 | 0.574 | 5 × 5 |
| build | n=4000 | 2.312 | ± 0.117 | 5.1% | 2.052 | 3.411 | 5 × 5 |
| chain | n=1000 | 0.711 | ± 0.030 | 4.3% | 0.639 | 0.893 | 5 × 5 |
| chain | n=4000 | 3.237 | ± 0.178 | 5.5% | 2.735 | 4.101 | 5 × 5 |
| randomBatch | n=1000 | 0.779 | ± 0.018 | 2.3% | 0.717 | 0.879 | 5 × 5 |
| randomBatch | n=4000 | 3.972 | ± 0.115 | 2.9% | 3.639 | 4.664 | 5 × 5 |
| star | n=1000 | 171.087 | ± 5.994 | 3.5% | 151.024 | 202.504 | 5 × 5 |
| star | n=4000 | 3266.638 | ± 50.031 | 1.5% | 3077.132 | 3527.469 | 5 × 5 |

## Noise and warmup

- Fork-to-fork spread of the means: 7–21% (worst: `build` n=4000, `chain` n=1000). The CIs above absorb this, but it
  is the dominant noise source; the machine was shared, so treat differences under ~10% as unresolved.
- Warmup is sufficient: the first warmup iteration is 1.3–3.3× slower than the last three, iterations 4–6 are already
  within 3–7% of iterations 8–10 (20% for `chain` n=4000, which keeps descending until about iteration 8), and the
  last three warmup iterations deviate from the measurement mean by −19% to +14% with no consistent sign, i.e. drift
  and noise rather than residual JIT work.

## Observations

- `build`, `chain` and `randomBatch` scale linearly (×4 in `n` gives ×5.0–5.3, the extra being GC and cache effects).
- `star` is quadratic: ×4 in `n` gives ×19. At n=4000 each incremental merge costs ≈ 0.8 ms versus ≈ 0.8 µs per
  merge in `chain`, purely from re-canonicalizing the 4000 parents of the large class on every merge.
