# E-graph benchmark results, 2026-08-28, laraserver4, cached hash

Experiment: `ENode` caches the hash of its canonical form in a field refreshed by the only two methods that change
the form (`recanonicalize` in `repair`, `restore` in `pop`), so the three congruence-table operations of a repair
do not rehash the arguments. Not adopted: see the comparison below. Code: the [while-loops commit](2026-08-28-laraserver4-while.md)
with that change.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 15 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-cached-hash.json](2026-08-28-laraserver4-cached-hash.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.280 | ± 0.004 | 1.3% | 0.268 | 0.306 | 5 × 5 |
| backtrack | n=4000 | 1.414 | ± 0.014 | 1.0% | 1.384 | 1.499 | 5 × 5 |
| build | n=1000 | 0.181 | ± 0.001 | 0.5% | 0.178 | 0.189 | 5 × 5 |
| build | n=4000 | 0.897 | ± 0.005 | 0.5% | 0.877 | 0.933 | 5 × 5 |
| chain | n=1000 | 0.113 | ± 0.001 | 1.2% | 0.108 | 0.119 | 5 × 5 |
| chain | n=4000 | 0.450 | ± 0.003 | 0.6% | 0.440 | 0.461 | 5 × 5 |
| explain | n=1000 | 0.249 | ± 0.003 | 1.4% | 0.240 | 0.266 | 5 × 5 |
| explain | n=4000 | 1.285 | ± 0.015 | 1.2% | 1.248 | 1.363 | 5 × 5 |
| randomBatch | n=1000 | 0.229 | ± 0.003 | 1.3% | 0.217 | 0.246 | 5 × 5 |
| randomBatch | n=4000 | 1.255 | ± 0.045 | 3.6% | 1.198 | 1.726 | 5 × 5 |
| star | n=1000 | 0.105 | ± 0.002 | 1.6% | 0.097 | 0.110 | 5 × 5 |
| star | n=4000 | 0.419 | ± 0.006 | 1.4% | 0.397 | 0.443 | 5 × 5 |

## Comparison with the previous run (recomputed hash)

| Benchmark | n | Recomputed hash (ms/op) | Cached hash (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| backtrack | 1000 | 0.274 ± 0.002 | 0.280 ± 0.004 | 0.98× |
| backtrack | 4000 | 1.439 ± 0.019 | 1.414 ± 0.014 | 1.02× |
| build | 1000 | 0.177 ± 0.002 | 0.181 ± 0.001 | 0.98× |
| build | 4000 | 0.901 ± 0.004 | 0.897 ± 0.005 | 1.00× |
| chain | 1000 | 0.114 ± 0.001 | 0.113 ± 0.001 | 1.01× |
| chain | 4000 | 0.454 ± 0.005 | 0.450 ± 0.003 | 1.01× |
| explain | 1000 | 0.233 ± 0.002 | 0.249 ± 0.003 | 0.94× |
| explain | 4000 | 1.282 ± 0.007 | 1.285 ± 0.015 | 1.00× |
| randomBatch | 1000 | 0.225 ± 0.005 | 0.229 ± 0.003 | 0.98× |
| randomBatch | 4000 | 1.226 ± 0.015 | 1.255 ± 0.045 | 0.98× |
| star | 1000 | 0.107 ± 0.001 | 0.105 ± 0.002 | 1.02× |
| star | 4000 | 0.423 ± 0.007 | 0.419 ± 0.006 | 1.01× |

No gain: 0.94–1.02×, within noise. With arguments of arity 1–2, hashing them costs about as much as reading and
maintaining a field. The simpler code (hash recomputed on demand) is kept.

## Noise and warmup

- CIs ≤ 2% except `randomBatch` n=4000 (3.6%, one spiking fork, also the 44% warmup deviation).
