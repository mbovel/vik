# E-graph benchmark results, 2026-08-28, laraserver4, backtracking

`EGraph` with `push`/`pop`: a trail of undo records (`NewClass`, `Added`, `Merged`, `Repaired`), written only
inside a level. New `backtrack` workload; the other workloads run at level 0 and should be unchanged. Code: the
`EGraph` of the commit that adds this file. Previous run: [case class](2026-08-28-laraserver4-case-class.md).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users; quiet during this run.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 13 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-backtrack.json](2026-08-28-laraserver4-backtrack.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.258 | ± 0.017 | 6.7% | 0.231 | 0.416 | 5 × 5 |
| backtrack | n=4000 | 1.441 | ± 0.135 | 9.4% | 1.286 | 2.960 | 5 × 5 |
| build | n=1000 | 0.143 | ± 0.002 | 1.7% | 0.137 | 0.162 | 5 × 5 |
| build | n=4000 | 0.821 | ± 0.006 | 0.7% | 0.798 | 0.863 | 5 × 5 |
| chain | n=1000 | 0.087 | ± 0.001 | 1.4% | 0.084 | 0.100 | 5 × 5 |
| chain | n=4000 | 0.347 | ± 0.002 | 0.6% | 0.338 | 0.360 | 5 × 5 |
| randomBatch | n=1000 | 0.192 | ± 0.002 | 0.8% | 0.186 | 0.199 | 5 × 5 |
| randomBatch | n=4000 | 1.163 | ± 0.016 | 1.4% | 1.102 | 1.218 | 5 × 5 |
| star | n=1000 | 0.077 | ± 0.001 | 1.4% | 0.074 | 0.083 | 5 × 5 |
| star | n=4000 | 0.321 | ± 0.003 | 1.0% | 0.313 | 0.337 | 5 × 5 |

## Comparison with the previous run (level-0 workloads)

| Benchmark | n | Case class (ms/op) | This run (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| build | 1000 | 0.154 ± 0.015 | 0.143 ± 0.002 | 1.08× |
| build | 4000 | 0.882 ± 0.044 | 0.821 ± 0.006 | 1.07× |
| chain | 1000 | 0.093 ± 0.004 | 0.087 ± 0.001 | 1.07× |
| chain | 4000 | 0.394 ± 0.030 | 0.347 ± 0.002 | 1.14× |
| randomBatch | 1000 | 0.191 ± 0.002 | 0.192 ± 0.002 | 1.00× |
| randomBatch | 4000 | 1.178 ± 0.023 | 1.163 ± 0.016 | 1.01× |
| star | 1000 | 0.075 ± 0.001 | 0.077 ± 0.001 | 0.98× |
| star | 4000 | 0.345 ± 0.038 | 0.321 ± 0.003 | 1.08× |

No regression from the trail: the level-0 workloads are equal or slightly faster (a quieter machine; the CIs of this
run are the tightest so far). `backtrack` minus `build` gives 0.12 ms for 500 and 0.62 ms for 2000 round trips of
`push`, one random merge, `rebuild`, `pop`, i.e. 0.23–0.31 µs per round trip: cheaper than a merge in `chain`,
because every `pop` restores the small classes.

## Noise and warmup

- Fork-to-fork spread 1–5% except `backtrack` n=4000, where one fork has a 2.9 ms iteration against 1.3–1.4 ms
  elsewhere (CI 9%).
- Warmup converges by iteration 5; the worst last-three-warmup vs measurement deviation (17%) is that same fork.
