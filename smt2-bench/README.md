# SMT-LIB parser benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.smt2.Parser`, in [Smt2Bench.scala](src/main/scala/net/bovel/smt2/bench/Smt2Bench.scala). The parsing of real benchmark files, and their solving, is measured in [smt-bench](../smt-bench/README.md).

## Workloads

| Benchmark | Workload | Exercises |
|---|---|---|
| `parse` | a generated QF_UF script with `n` (1000 or 10000) assertions over one sort, three functions and fifty constants, using `let`, `ite`, `distinct`, `=>`, `or`, `not` and `=` | the S-expression lexer, then the sort-directed translation into `smt` terms and formulas (`let` bindings, `ite` lowering, `distinct` expansion) |

## Running

From the repository root (see [egraph-bench/README.md](../egraph-bench/README.md) for the setup of `jmh2md.py`):

```sh
sbt "smt2Bench/Jmh/run -rf json -rff target/jmh-result.json"
egraph-bench/.venv/bin/python egraph-bench/jmh2md.py target/jmh-result.json
```

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

## Current results

(none yet)

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
