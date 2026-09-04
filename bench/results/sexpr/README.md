# S-expression parser benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.sexpr.SExprParser`, in [SExprBench.scala](../../src/main/scala/net/bovel/bench/sexpr/SExprBench.scala).

## Workloads

| Benchmark | Workload | Exercises |
|---|---|---|
| `parse` | a generated text of `n` (1000 or 10000) S-expressions, nested applications of depth 3 over symbols, numerals, quoted symbols and keywords, with a comment every ten expressions | the regex lexer and the tree construction |

## Running

From the repository root (see [bench/README.md](../../README.md) for the setup of `jmh2md.py`):

```sh
sbt "bench/Jmh/run -rf json -rff target/jmh-result.json"
bench/.venv/bin/python bench/jmh2md.py target/jmh-result.json
```

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-29. Setup and noise: [results/2026-08-29-laraserver4.md](2026-08-29-laraserver4.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| parse | n=1000 | 2.571 | ± 0.075 | 2.9% | 2.206 | 2.912 | 5 × 5 |
| parse | n=10000 | 25.009 | ± 0.473 | 1.9% | 24.034 | 28.209 | 5 × 5 |

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-29, laraserver4](2026-08-29-laraserver4.md) — First full run of `SExprBench` (regex lexer, tree construction)
- [2026-09-04, laraserver4, numbers read by `BigDecimal`](2026-09-04-laraserver4-bigdecimal.md) — the lexer telling a numeral from a decimal in one pass instead of five (kept for the reading): 0.98× and 1.03×, a wash
