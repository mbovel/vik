# E-graph benchmark results, 2026-08-29, laraserver4

With the event log of merges and disequalities for theory propagation and the linear lowest common ancestor in `explain`; compared with the last record (proof production, `while` loops in `add`).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data:
  [2026-08-29-laraserver4.json](2026-08-29-laraserver4.json).

Compared with [2026-08-28-laraserver4-while](2026-08-28-laraserver4-while.json):

| Benchmark | Params | Before (ms/op) | After (ms/op) | Before / after |
|---|---|---:|---:|---:|
| `backtrack` | n=1000 | 0.274 ± 0.002 | 0.287 ± 0.008 | 0.95× |
| `backtrack` | n=4000 | 1.439 ± 0.019 | 1.504 ± 0.035 | 0.96× |
| `build` | n=1000 | 0.177 ± 0.002 | 0.178 ± 0.002 | 0.99× |
| `build` | n=4000 | 0.901 ± 0.004 | 1.021 ± 0.100 | 0.88× |
| `chain` | n=1000 | 0.114 ± 0.001 | 0.121 ± 0.003 | 0.94× |
| `chain` | n=4000 | 0.454 ± 0.005 | 0.479 ± 0.004 | 0.95× |
| `explain` | n=1000 | 0.233 ± 0.002 | 0.243 ± 0.003 | 0.96× |
| `explain` | n=4000 | 1.282 ± 0.007 | 1.290 ± 0.016 | 0.99× |
| `randomBatch` | n=1000 | 0.225 ± 0.005 | 0.226 ± 0.002 | 0.99× |
| `randomBatch` | n=4000 | 1.226 ± 0.015 | 1.254 ± 0.012 | 0.98× |
| `star` | n=1000 | 0.107 ± 0.001 | 0.109 ± 0.002 | 0.98× |
| `star` | n=4000 | 0.423 ± 0.007 | 0.435 ± 0.006 | 0.97× |

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.287 | ± 0.008 | 2.9% | 0.272 | 0.368 | 5 × 5 |
| backtrack | n=4000 | 1.504 | ± 0.035 | 2.4% | 1.424 | 1.774 | 5 × 5 |
| build | n=1000 | 0.178 | ± 0.002 | 1.1% | 0.171 | 0.189 | 5 × 5 |
| build | n=4000 | 1.021 | ± 0.100 | 9.8% | 0.877 | 2.059 | 5 × 5 |
| chain | n=1000 | 0.121 | ± 0.003 | 2.1% | 0.116 | 0.142 | 5 × 5 |
| chain | n=4000 | 0.479 | ± 0.004 | 0.9% | 0.461 | 0.502 | 5 × 5 |
| explain | n=1000 | 0.243 | ± 0.003 | 1.4% | 0.231 | 0.268 | 5 × 5 |
| explain | n=4000 | 1.290 | ± 0.016 | 1.2% | 1.248 | 1.382 | 5 × 5 |
| randomBatch | n=1000 | 0.226 | ± 0.002 | 1.0% | 0.219 | 0.246 | 5 × 5 |
| randomBatch | n=4000 | 1.254 | ± 0.012 | 1.0% | 1.227 | 1.327 | 5 × 5 |
| star | n=1000 | 0.109 | ± 0.002 | 1.4% | 0.103 | 0.116 | 5 × 5 |
| star | n=4000 | 0.435 | ± 0.006 | 1.3% | 0.415 | 0.459 | 5 × 5 |
