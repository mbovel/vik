# E-graph benchmark results, 2026-08-28, laraserver4, dense arrays

First optimized `EGraph`: dense arrays indexed by `Int` class ids (`repr`, circular member list `next`, `size`,
`uses`), e-nodes as plain objects carrying their class and canonicalized in place, hashcons `HashMap[ENode, ENode]`
(no boxed values), and `merge` re-canonicalizing only the parents of the smaller class. Code: the `EGraph` of the
commit that adds this file. Baseline for comparison: [2026-08-28-laraserver4.md](2026-08-28-laraserver4.md).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 10 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-arrays.json](2026-08-28-laraserver4-arrays.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| build | n=1000 | 0.165 | ± 0.009 | 5.2% | 0.143 | 0.227 | 5 × 5 |
| build | n=4000 | 1.014 | ± 0.036 | 3.6% | 0.860 | 1.164 | 5 × 5 |
| chain | n=1000 | 0.114 | ± 0.009 | 7.7% | 0.092 | 0.171 | 5 × 5 |
| chain | n=4000 | 0.459 | ± 0.035 | 7.7% | 0.371 | 0.756 | 5 × 5 |
| randomBatch | n=1000 | 0.205 | ± 0.006 | 2.8% | 0.194 | 0.249 | 5 × 5 |
| randomBatch | n=4000 | 1.209 | ± 0.019 | 1.5% | 1.121 | 1.286 | 5 × 5 |
| star | n=1000 | 0.080 | ± 0.001 | 1.1% | 0.078 | 0.084 | 5 × 5 |
| star | n=4000 | 0.415 | ± 0.056 | 13.4% | 0.319 | 0.829 | 5 × 5 |

## Comparison with the baseline

| Benchmark | n | Baseline (ms/op) | This run (ms/op) | Speedup |
|---|---:|---:|---:|---:|
| build | 1000 | 0.436 ± 0.021 | 0.165 ± 0.009 | 2.6× |
| build | 4000 | 2.312 ± 0.117 | 1.014 ± 0.036 | 2.3× |
| chain | 1000 | 0.711 ± 0.030 | 0.114 ± 0.009 | 6.3× |
| chain | 4000 | 3.237 ± 0.178 | 0.459 ± 0.035 | 7.1× |
| randomBatch | 1000 | 0.779 ± 0.018 | 0.205 ± 0.006 | 3.8× |
| randomBatch | 4000 | 3.972 ± 0.115 | 1.209 ± 0.019 | 3.3× |
| star | 1000 | 171.087 ± 5.994 | 0.080 ± 0.001 | 2,135.7× |
| star | 4000 | 3266.638 ± 50.031 | 0.415 ± 0.056 | 7,878.2× |

- `star` is now linear (×5.2 for ×4 in `n`) and cheaper per merge than `chain`; the four-digit factors are the
  quadratic term disappearing.
- The 2.3–7× on the linear workloads is the constant-factor work: dense indexing instead of `LongMap`, no tuples or
  boxed map values, in-place canonicalization. `chain` gains most (merge-dominated), `build` least (hashcons-dominated:
  still one node object and one `HashMap` entry per `add`).

## Noise and warmup

- Operations are now 0.1–1 ms, so interference from the shared machine shows more: fork-to-fork spread is 5–32%,
  and 46% for `star` n=4000, where three forks sit flat at 0.33–0.35 ms while two others have single iterations at
  0.6–0.8 ms (its mean of 0.415 ms is therefore pessimistic).
- Warmup is still sufficient: the first warmup iteration is 1.9–3× slower than the last three, and convergence
  happens by iteration 6; the larger last-three-warmup vs measurement deviations (up to 31%, `chain` and `star` at
  n=4000) are the same interference spikes, in both directions.
