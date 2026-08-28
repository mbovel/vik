# E-graph benchmark results, 2026-08-28, laraserver4, while loops

Same `EGraph` as the [simplifications run](2026-08-28-laraserver4-simplified.md), with the two `for arg <-
node.args` loops (in `add` and in the undo of an `add`) replaced by `while` loops: `for` over an `Array[Int]` goes
through the generic `foreach` and boxes every element. Code: the `EGraph` of the commit that adds this file.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users; quiet during this run.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 15 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-while.json](2026-08-28-laraserver4-while.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.274 | ± 0.002 | 0.7% | 0.267 | 0.281 | 5 × 5 |
| backtrack | n=4000 | 1.439 | ± 0.019 | 1.3% | 1.392 | 1.525 | 5 × 5 |
| build | n=1000 | 0.177 | ± 0.002 | 1.0% | 0.168 | 0.183 | 5 × 5 |
| build | n=4000 | 0.901 | ± 0.004 | 0.5% | 0.878 | 0.914 | 5 × 5 |
| chain | n=1000 | 0.114 | ± 0.001 | 1.1% | 0.109 | 0.121 | 5 × 5 |
| chain | n=4000 | 0.454 | ± 0.005 | 1.0% | 0.445 | 0.491 | 5 × 5 |
| explain | n=1000 | 0.233 | ± 0.002 | 0.7% | 0.226 | 0.238 | 5 × 5 |
| explain | n=4000 | 1.282 | ± 0.007 | 0.6% | 1.249 | 1.309 | 5 × 5 |
| randomBatch | n=1000 | 0.225 | ± 0.005 | 2.2% | 0.218 | 0.262 | 5 × 5 |
| randomBatch | n=4000 | 1.226 | ± 0.015 | 1.3% | 1.188 | 1.294 | 5 × 5 |
| star | n=1000 | 0.107 | ± 0.001 | 1.3% | 0.101 | 0.113 | 5 × 5 |
| star | n=4000 | 0.423 | ± 0.007 | 1.6% | 0.402 | 0.452 | 5 × 5 |

## Comparison with the previous run

| Benchmark | n | For loop (ms/op) | While loop (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| backtrack | 1000 | 0.287 ± 0.003 | 0.274 ± 0.002 | 1.05× |
| backtrack | 4000 | 1.454 ± 0.011 | 1.439 ± 0.019 | 1.01× |
| build | 1000 | 0.184 ± 0.003 | 0.177 ± 0.002 | 1.04× |
| build | 4000 | 0.953 ± 0.023 | 0.901 ± 0.004 | 1.06× |
| chain | 1000 | 0.121 ± 0.001 | 0.114 ± 0.001 | 1.06× |
| chain | 4000 | 0.490 ± 0.005 | 0.454 ± 0.005 | 1.08× |
| explain | 1000 | 0.255 ± 0.003 | 0.233 ± 0.002 | 1.09× |
| explain | 4000 | 1.387 ± 0.018 | 1.282 ± 0.007 | 1.08× |
| randomBatch | 1000 | 0.247 ± 0.014 | 0.225 ± 0.005 | 1.10× |
| randomBatch | 4000 | 1.259 ± 0.011 | 1.226 ± 0.015 | 1.03× |
| star | 1000 | 0.104 ± 0.001 | 0.107 ± 0.001 | 0.98× |
| star | 4000 | 0.435 ± 0.007 | 0.423 ± 0.007 | 1.03× |

3–10% faster on most workloads, with disjoint confidence intervals on `build` n=4000, `chain`, `explain` and
`randomBatch` n=1000; `star` unchanged.

## Noise and warmup

- The quietest run so far: CIs ≤ 2.2%, worst last-three-warmup vs measurement deviation 9%.
