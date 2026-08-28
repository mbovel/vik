# CDCL(T) solver benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.cdclt.Solver`, in [SolverBench.scala](src/main/scala/net/bovel/cdclt/bench/SolverBench.scala). Pure SAT for now (no theory); theory workloads come with the `smt` module.

## Workloads

Each benchmark builds and solves fresh instances. `n` is 30 or 60.

| Benchmark | Workload | Exercises |
|---|---|---|
| `random3Sat` | ten random 3-SAT instances with `n` variables at clause/variable ratio 4.2 (near the threshold: a mix of sat and unsat) | propagation, learning, backjumping on unstructured instances |
| `pigeonhole` | `n / 8` pigeons into one hole less: unsat, exponential for resolution | learning on a structured, hard instance |

## Running

From the repository root (see [egraph-bench/README.md](../egraph-bench/README.md) for the setup of `jmh2md.py`):

```sh
sbt "cdcltBench/Jmh/run -rf json -rff target/jmh-result.json"
egraph-bench/.venv/bin/python egraph-bench/jmh2md.py target/jmh-result.json
```

Configuration: 5 forks, 10 warmup and 5 measurement iterations of 1 s each, average time per operation.

## Current results

(none yet)

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
