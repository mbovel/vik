# E-graph benchmark results, 2026-08-28, laraserver4, proof production

`EGraph` with explanations: every application gets its own node keeping its original arguments (`App`), congruences
become justified merges (at `add` and in `rebuild`), each merge records an `Edge` of the union tree with a
`Justification` and a timestamp, and `explain` reads the union tree (imperative version: hash set of ancestors,
two scans for the most recent edge). New `explain` workload. Code: the `EGraph` of the commit that adds this file.
Previous run: [backtracking](2026-08-28-laraserver4-backtrack.md).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 15 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-explain.json](2026-08-28-laraserver4-explain.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.310 | ± 0.023 | 7.6% | 0.273 | 0.546 | 5 × 5 |
| backtrack | n=4000 | 1.485 | ± 0.027 | 1.8% | 1.438 | 1.734 | 5 × 5 |
| build | n=1000 | 0.190 | ± 0.015 | 8.1% | 0.175 | 0.350 | 5 × 5 |
| build | n=4000 | 1.019 | ± 0.057 | 5.6% | 0.935 | 1.489 | 5 × 5 |
| chain | n=1000 | 0.119 | ± 0.001 | 0.6% | 0.116 | 0.122 | 5 × 5 |
| chain | n=4000 | 0.478 | ± 0.003 | 0.6% | 0.469 | 0.491 | 5 × 5 |
| explain | n=1000 | 0.265 | ± 0.002 | 0.8% | 0.258 | 0.286 | 5 × 5 |
| explain | n=4000 | 1.427 | ± 0.018 | 1.3% | 1.370 | 1.521 | 5 × 5 |
| randomBatch | n=1000 | 0.247 | ± 0.004 | 1.5% | 0.236 | 0.269 | 5 × 5 |
| randomBatch | n=4000 | 1.328 | ± 0.021 | 1.6% | 1.244 | 1.404 | 5 × 5 |
| star | n=1000 | 0.108 | ± 0.002 | 1.6% | 0.100 | 0.115 | 5 × 5 |
| star | n=4000 | 0.437 | ± 0.007 | 1.6% | 0.420 | 0.473 | 5 × 5 |

## Comparison with the previous run

| Benchmark | n | Backtracking (ms/op) | Proof production (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| backtrack | 1000 | 0.258 ± 0.017 | 0.310 ± 0.023 | 0.83× |
| backtrack | 4000 | 1.441 ± 0.135 | 1.485 ± 0.027 | 0.97× |
| build | 1000 | 0.143 ± 0.002 | 0.190 ± 0.015 | 0.75× |
| build | 4000 | 0.821 ± 0.006 | 1.019 ± 0.057 | 0.81× |
| chain | 1000 | 0.087 ± 0.001 | 0.119 ± 0.001 | 0.73× |
| chain | 4000 | 0.347 ± 0.002 | 0.478 ± 0.003 | 0.73× |
| randomBatch | 1000 | 0.192 ± 0.002 | 0.247 ± 0.004 | 0.78× |
| randomBatch | 4000 | 1.163 ± 0.016 | 1.328 ± 0.021 | 0.88× |
| star | 1000 | 0.077 ± 0.001 | 0.108 ± 0.002 | 0.71× |
| star | 4000 | 0.321 ± 0.003 | 0.437 ± 0.007 | 0.73× |

The cost of proof production on the workloads that do not ask for explanations is 12–29%: one more array per node
(the original arguments), one `Edge` and one `Justification` per merge, and a congruence lookup at every `add`. A
first version with a second hash table for syntactic dedup in `add` cost 1.5–1.9× instead; dedup now belongs to the
caller (`EGraphBuilder`, or term hash-consing in a solver).

`explain` is `randomBatch` plus explanations of every pair of consecutive equal ids: they add 0.02 ms (n=1000) and
0.10 ms (n=4000) to `randomBatch`'s 0.25 and 1.33 ms.

## Noise and warmup

- Fork-to-fork spread is small (CIs ≤ 2%) except `build` and `backtrack` at n=1000, where single iterations spike to
  2× (interference; CIs 8%).
- Warmup converges by iteration 5; the worst last-three-warmup vs measurement deviation (92%) is one such spike.
