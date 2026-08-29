# S-expression parser benchmark results, 2026-08-29, laraserver4

First full run of `SExprBench` (regex lexer, tree construction).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- OpenJDK 17.0.20, Scala 3.9.0, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 5 forks, 10 warmup + 5 measurement iterations of 1 s, average time per operation, one thread.
- 95% confidence interval from the 25 measurement samples (5 forks × 5 iterations), Student's t. Raw data:
  [2026-08-29-laraserver4.json](2026-08-29-laraserver4.json).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| parse | n=1000 | 2.571 | ± 0.075 | 2.9% | 2.206 | 2.912 | 5 × 5 |
| parse | n=10000 | 25.009 | ± 0.473 | 1.9% | 24.034 | 28.209 | 5 × 5 |
