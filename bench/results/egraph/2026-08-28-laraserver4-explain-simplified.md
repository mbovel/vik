# E-graph benchmark results, 2026-08-28, laraserver4, simplified explain

Same `EGraph` as the [proof-production run](2026-08-28-laraserver4-explain.md), with `explain` rewritten for
readability: the two paths to the root, the lowest common ancestor as the first node of one path found on the
other, and the most recent edge below it by `maxBy`, instead of a hash set of ancestors and two hand-written scans.
Code: the `EGraph` of the commit that adds this file.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 15 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-explain-simplified.json](2026-08-28-laraserver4-explain-simplified.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.293 | ± 0.005 | 1.6% | 0.279 | 0.331 | 5 × 5 |
| backtrack | n=4000 | 1.584 | ± 0.085 | 5.3% | 1.443 | 2.154 | 5 × 5 |
| build | n=1000 | 0.180 | ± 0.003 | 1.7% | 0.172 | 0.197 | 5 × 5 |
| build | n=4000 | 0.953 | ± 0.009 | 1.0% | 0.929 | 1.035 | 5 × 5 |
| chain | n=1000 | 0.120 | ± 0.001 | 1.1% | 0.115 | 0.127 | 5 × 5 |
| chain | n=4000 | 0.484 | ± 0.005 | 1.1% | 0.470 | 0.522 | 5 × 5 |
| explain | n=1000 | 0.262 | ± 0.002 | 0.7% | 0.255 | 0.271 | 5 × 5 |
| explain | n=4000 | 1.395 | ± 0.017 | 1.2% | 1.335 | 1.485 | 5 × 5 |
| randomBatch | n=1000 | 0.240 | ± 0.003 | 1.2% | 0.232 | 0.257 | 5 × 5 |
| randomBatch | n=4000 | 1.356 | ± 0.029 | 2.1% | 1.283 | 1.582 | 5 × 5 |
| star | n=1000 | 0.105 | ± 0.001 | 1.3% | 0.100 | 0.114 | 5 × 5 |
| star | n=4000 | 0.443 | ± 0.007 | 1.7% | 0.419 | 0.471 | 5 × 5 |

## Comparison with the imperative explain

| Benchmark | n | Imperative explain (ms/op) | Declarative explain (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| backtrack | 1000 | 0.310 ± 0.023 | 0.293 ± 0.005 | 1.06× |
| backtrack | 4000 | 1.485 ± 0.027 | 1.584 ± 0.085 | 0.94× |
| build | 1000 | 0.190 ± 0.015 | 0.180 ± 0.003 | 1.05× |
| build | 4000 | 1.019 ± 0.057 | 0.953 ± 0.009 | 1.07× |
| chain | 1000 | 0.119 ± 0.001 | 0.120 ± 0.001 | 0.99× |
| chain | 4000 | 0.478 ± 0.003 | 0.484 ± 0.005 | 0.99× |
| explain | 1000 | 0.265 ± 0.002 | 0.262 ± 0.002 | 1.01× |
| explain | 4000 | 1.427 ± 0.018 | 1.395 ± 0.017 | 1.02× |
| randomBatch | 1000 | 0.247 ± 0.004 | 0.240 ± 0.003 | 1.03× |
| randomBatch | 4000 | 1.328 ± 0.021 | 1.356 ± 0.029 | 0.98× |
| star | 1000 | 0.108 ± 0.002 | 0.105 ± 0.001 | 1.02× |
| star | 4000 | 0.437 ± 0.007 | 0.443 ± 0.007 | 0.99× |

No measurable difference: `explain` is 1.01–1.02× with overlapping intervals, and the other workloads, which do not
call it, are within noise. The readable version is kept.

## Noise and warmup

- CIs ≤ 2% except `backtrack` n=4000 (5%, one slow fork). Warmup converges by iteration 5; the worst last-three-warmup
  vs measurement deviation (45%) is that fork.
