# CDCL(T) solver benchmark results, 2026-08-28, laraserver4, with the 5-SAT workload

Occurrence-list propagation, before two watched literals; commit `448494c` (the first baseline plus the
implied-literal-first convention, the opaque `Lit` and the `random5Sat` workload). The reference point for the
optimizations that follow.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users. This run is 2–5× faster than
  the [first baseline](2026-08-28-laraserver4.md) on the common workloads although the code differs only cosmetically:
  the machine's load varies between runs, so only consecutive runs are compared.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread. Total 8 min.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data: [2026-08-28-laraserver4-5sat.json](2026-08-28-laraserver4-5sat.json).

## Results

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.001 | ± 0.000 | 3.2% | 0.001 | 0.001 | 5 × 5 |
| pigeonhole | n=60 | 0.496 | ± 0.009 | 1.9% | 0.455 | 0.537 | 5 × 5 |
| random3Sat | n=30 | 0.299 | ± 0.007 | 2.3% | 0.278 | 0.332 | 5 × 5 |
| random3Sat | n=60 | 6.302 | ± 0.069 | 1.1% | 6.028 | 6.618 | 5 × 5 |
| random5Sat | n=30 | 4.235 | ± 0.069 | 1.6% | 3.968 | 4.548 | 5 × 5 |
| random5Sat | n=60 | 301.057 | ± 3.134 | 1.0% | 290.443 | 313.452 | 5 × 5 |

`random3Sat` and `random5Sat` are ten instances per operation. `pigeonhole` n=30 is 3 pigeons, trivial; n=60 is 7.

## Noise and warmup

- CIs ≤ 3.2%.
