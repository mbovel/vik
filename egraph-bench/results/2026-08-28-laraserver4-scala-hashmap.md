# E-graph benchmark results, 2026-08-28, laraserver4, Scala HashMap hashcons

Experiment: the hashcons as Scala's `mutable.HashMap[ENode, ENode]` (`getOrElse(node, null)`, `update`, `-=`)
instead of `java.util.HashMap`, everything else as in the [case-class run](2026-08-28-laraserver4-case-class.md).
Not adopted: see the comparison below. Code: the case-class commit with the `memo` lines swapped.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 10 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-scala-hashmap.json](2026-08-28-laraserver4-scala-hashmap.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| build | n=1000 | 0.149 | ± 0.004 | 2.4% | 0.141 | 0.170 | 5 × 5 |
| build | n=4000 | 0.917 | ± 0.009 | 1.0% | 0.901 | 0.979 | 5 × 5 |
| chain | n=1000 | 0.088 | ± 0.000 | 0.4% | 0.087 | 0.090 | 5 × 5 |
| chain | n=4000 | 0.421 | ± 0.043 | 10.3% | 0.361 | 0.808 | 5 × 5 |
| randomBatch | n=1000 | 0.201 | ± 0.004 | 1.9% | 0.189 | 0.224 | 5 × 5 |
| randomBatch | n=4000 | 1.231 | ± 0.033 | 2.7% | 1.180 | 1.569 | 5 × 5 |
| star | n=1000 | 0.077 | ± 0.001 | 1.5% | 0.074 | 0.081 | 5 × 5 |
| star | n=4000 | 0.322 | ± 0.004 | 1.3% | 0.309 | 0.342 | 5 × 5 |

## Comparison with java.util.HashMap (case-class run)

| Benchmark | n | java.util.HashMap (ms/op) | Scala HashMap (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| build | 1000 | 0.154 ± 0.015 | 0.149 ± 0.004 | 1.03× |
| build | 4000 | 0.882 ± 0.044 | 0.917 ± 0.009 | 0.96× |
| chain | 1000 | 0.093 ± 0.004 | 0.088 ± 0.000 | 1.05× |
| chain | 4000 | 0.394 ± 0.030 | 0.421 ± 0.043 | 0.94× |
| randomBatch | 1000 | 0.191 ± 0.002 | 0.201 ± 0.004 | 0.95× |
| randomBatch | 4000 | 1.178 ± 0.023 | 1.231 ± 0.033 | 0.96× |
| star | 1000 | 0.075 ± 0.001 | 0.077 ± 0.001 | 0.97× |
| star | 4000 | 0.345 ± 0.038 | 0.322 ± 0.004 | 1.07× |

`randomBatch` is about 5% slower with the Scala map at both sizes, with disjoint confidence intervals; every other
row is equal within its interval (ratios 0.94–1.07×). `java.util.HashMap` is kept.

## Noise and warmup

- Fork-to-fork spread is small this time except `chain` n=4000 (one fork at 0.5 ms against 0.39–0.41 ms elsewhere).
- Warmup converges by iteration 6; the worst last-three-warmup vs measurement deviation (34%) is that same fork.
