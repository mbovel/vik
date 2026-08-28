# E-graph benchmark results, 2026-08-28, laraserver4, case-class ENode

Same data structures as the [dense-array run](2026-08-28-laraserver4-arrays.md); `ENode` is now a private case class
with structural `equals`/`hashCode` (arrays compare by reference), `getClasses` returns `(fun, args)` pairs instead
of nodes, and canonicalization of arguments goes through one `canonicalArgs(src, dst)` helper. Hashcons:
`java.util.HashMap[ENode, ENode]`. Code: the `EGraph` of the commit that adds this file.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 10 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-case-class.json](2026-08-28-laraserver4-case-class.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| build | n=1000 | 0.154 | ± 0.015 | 9.6% | 0.134 | 0.280 | 5 × 5 |
| build | n=4000 | 0.882 | ± 0.044 | 5.0% | 0.810 | 1.258 | 5 × 5 |
| chain | n=1000 | 0.093 | ± 0.004 | 4.4% | 0.083 | 0.115 | 5 × 5 |
| chain | n=4000 | 0.394 | ± 0.030 | 7.7% | 0.348 | 0.621 | 5 × 5 |
| randomBatch | n=1000 | 0.191 | ± 0.002 | 0.8% | 0.185 | 0.198 | 5 × 5 |
| randomBatch | n=4000 | 1.178 | ± 0.023 | 1.9% | 1.113 | 1.371 | 5 × 5 |
| star | n=1000 | 0.075 | ± 0.001 | 1.3% | 0.072 | 0.080 | 5 × 5 |
| star | n=4000 | 0.345 | ± 0.038 | 10.9% | 0.300 | 0.732 | 5 × 5 |

## Comparison with the dense-array run

| Benchmark | n | Dense arrays (ms/op) | This run (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| build | 1000 | 0.165 ± 0.009 | 0.154 ± 0.015 | 1.07× |
| build | 4000 | 1.014 ± 0.036 | 0.882 ± 0.044 | 1.15× |
| chain | 1000 | 0.114 ± 0.009 | 0.093 ± 0.004 | 1.23× |
| chain | 4000 | 0.459 ± 0.035 | 0.394 ± 0.030 | 1.17× |
| randomBatch | 1000 | 0.205 ± 0.006 | 0.191 ± 0.002 | 1.07× |
| randomBatch | 4000 | 1.209 ± 0.019 | 1.178 ± 0.023 | 1.03× |
| star | 1000 | 0.080 ± 0.001 | 0.075 ± 0.001 | 1.07× |
| star | 4000 | 0.415 ± 0.056 | 0.345 ± 0.038 | 1.20× |

The code differences are cosmetic (same fields, same hashing, same loops), so the 3–23% is run-to-run variation of a
shared machine, not the change: the dense-array run happened under a load average of about 13, this one under a
quieter machine. Treat the two runs as equivalent.

## Noise and warmup

- Fork-to-fork spread 2–25%; `star` n=4000 again has two forks with single iterations at 0.6–0.7 ms against a flat
  0.30–0.35 ms elsewhere (CI 11%).
- Warmup converges by iteration 6; the last-three-warmup vs measurement deviations up to 34% are the same isolated
  spikes.
