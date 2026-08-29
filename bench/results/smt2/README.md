# SMT-LIB parser benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.smt2.Parser`, in [Smt2Bench.scala](../../src/main/scala/net/bovel/bench/Smt2Bench.scala). The parsing of real benchmark files, and their solving, is measured in [the smt results](../smt/README.md).

## Workloads

| Benchmark | Workload | Exercises |
|---|---|---|
| `parse` | a generated QF_UF script with `n` (1000 or 10000) assertions over one sort, three functions and fifty constants, using `let`, `ite`, `distinct`, `=>`, `or`, `not` and `=` | the S-expression lexer, then the sort-directed translation into `smt` terms and formulas (`let` bindings, `ite` lowering, `distinct` expansion) |

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
| parse | n=1000 | 3.735 | ± 0.047 | 1.2% | 3.520 | 3.954 | 5 × 5 |
| parse | n=10000 | 53.931 | ± 4.668 | 8.7% | 44.749 | 95.761 | 5 × 5 |

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-29, laraserver4](2026-08-29-laraserver4.md) — First full run of `Smt2Bench` (S-expression lexer, sort-directed translation)
