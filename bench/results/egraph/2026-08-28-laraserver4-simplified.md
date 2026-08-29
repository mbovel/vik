# E-graph benchmark results, 2026-08-28, laraserver4, simplifications

Same `EGraph` as the [simplified-explain run](2026-08-28-laraserver4-explain-simplified.md) after a readability
pass: `add` and `repair` share `insertOrMerge` (one `putIfAbsent` instead of `get` + `put`), congruence edges are no
longer flipped, `cls` is a constructor field, lists moved by `merge` are appended and never shared (no sentinels in
the undo record), a `forbidding` helper, `Array.tabulate` in `add`. Code: the `EGraph` of the commit that adds this file.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 15 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-simplified.json](2026-08-28-laraserver4-simplified.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.287 | ± 0.003 | 1.0% | 0.278 | 0.299 | 5 × 5 |
| backtrack | n=4000 | 1.454 | ± 0.011 | 0.8% | 1.426 | 1.549 | 5 × 5 |
| build | n=1000 | 0.184 | ± 0.003 | 1.5% | 0.174 | 0.199 | 5 × 5 |
| build | n=4000 | 0.953 | ± 0.023 | 2.4% | 0.908 | 1.172 | 5 × 5 |
| chain | n=1000 | 0.121 | ± 0.001 | 0.7% | 0.119 | 0.128 | 5 × 5 |
| chain | n=4000 | 0.490 | ± 0.005 | 1.0% | 0.469 | 0.510 | 5 × 5 |
| explain | n=1000 | 0.255 | ± 0.003 | 1.2% | 0.243 | 0.268 | 5 × 5 |
| explain | n=4000 | 1.387 | ± 0.018 | 1.3% | 1.333 | 1.496 | 5 × 5 |
| randomBatch | n=1000 | 0.247 | ± 0.014 | 5.7% | 0.227 | 0.393 | 5 × 5 |
| randomBatch | n=4000 | 1.259 | ± 0.011 | 0.8% | 1.227 | 1.305 | 5 × 5 |
| star | n=1000 | 0.104 | ± 0.001 | 1.1% | 0.100 | 0.110 | 5 × 5 |
| star | n=4000 | 0.435 | ± 0.007 | 1.7% | 0.408 | 0.464 | 5 × 5 |

## Comparison with the previous run

| Benchmark | n | Before (ms/op) | Simplified (ms/op) | Ratio |
|---|---:|---:|---:|---:|
| backtrack | 1000 | 0.293 ± 0.005 | 0.287 ± 0.003 | 1.02× |
| backtrack | 4000 | 1.584 ± 0.085 | 1.454 ± 0.011 | 1.09× |
| build | 1000 | 0.180 ± 0.003 | 0.184 ± 0.003 | 0.98× |
| build | 4000 | 0.953 ± 0.009 | 0.953 ± 0.023 | 1.00× |
| chain | 1000 | 0.120 ± 0.001 | 0.121 ± 0.001 | 0.99× |
| chain | 4000 | 0.484 ± 0.005 | 0.490 ± 0.005 | 0.99× |
| explain | 1000 | 0.262 ± 0.002 | 0.255 ± 0.003 | 1.03× |
| explain | 4000 | 1.395 ± 0.017 | 1.387 ± 0.018 | 1.01× |
| randomBatch | 1000 | 0.240 ± 0.003 | 0.247 ± 0.014 | 0.97× |
| randomBatch | 4000 | 1.356 ± 0.029 | 1.259 ± 0.011 | 1.08× |
| star | 1000 | 0.105 ± 0.001 | 0.104 ± 0.001 | 1.01× |
| star | 4000 | 0.443 ± 0.007 | 0.435 ± 0.007 | 1.02× |

No regression: 0.97–1.09×, within noise (the two 1.08–1.09× rows had the noisier intervals before).

## Noise and warmup

- CIs ≤ 2.5% except `randomBatch` n=1000 (6%, one spiking fork). Warmup converges by iteration 5; the worst
  last-three-warmup vs measurement deviation (74%) is that fork.
