# CDCL(T) solver benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.cdclt.Solver`, in [SolverBench.scala](src/main/scala/net/bovel/cdclt/bench/SolverBench.scala). Pure SAT for now (no theory); theory workloads come with the `smt` module.

## Workloads

Each benchmark builds and solves fresh instances. `n` is 30 or 60.

| Benchmark | Workload | Exercises |
|---|---|---|
| `random3Sat` | ten random 3-SAT instances with `n` variables at clause/variable ratio 4.2 (near the threshold: a mix of sat and unsat) | propagation, learning, backjumping on unstructured instances |
| `random5Sat` | ten random 5-SAT instances with `n` variables at ratio 15 (below the threshold, mostly sat): long clauses, many per variable | propagation cost per clause visit (where watched literals pay off) |
| `pigeonhole` | `n / 8` pigeons into one hole less: unsat, exponential for resolution | learning on a structured, hard instance |

## Running

From the repository root (see [egraph-bench/README.md](../egraph-bench/README.md) for the setup of `jmh2md.py`):

```sh
sbt "cdcltBench/Jmh/run -rf json -rff target/jmh-result.json"
egraph-bench/.venv/bin/python egraph-bench/jmh2md.py target/jmh-result.json
```

Configuration: 5 forks, 10 warmup and 5 measurement iterations of 1 s each, average time per operation.

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, unoptimized baseline. Setup and noise: [results/2026-08-28-laraserver4.md](results/2026-08-28-laraserver4.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.002 | ± 0.000 | 2.2% | 0.002 | 0.002 | 5 × 5 |
| pigeonhole | n=60 | 2.903 | ± 0.041 | 1.4% | 2.789 | 3.075 | 5 × 5 |
| random3Sat | n=30 | 1.599 | ± 0.023 | 1.4% | 1.526 | 1.693 | 5 × 5 |
| random3Sat | n=60 | 15.537 | ± 0.250 | 1.6% | 14.690 | 16.627 | 5 × 5 |

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — unoptimized baseline ([json](results/2026-08-28-laraserver4.json))
