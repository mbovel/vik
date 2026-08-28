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

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, with two watched literals. Setup and noise: [results/2026-08-28-laraserver4-2wl.md](results/2026-08-28-laraserver4-2wl.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.001 | ± 0.000 | 5.0% | 0.001 | 0.001 | 5 × 5 |
| pigeonhole | n=60 | 0.239 | ± 0.005 | 2.2% | 0.216 | 0.260 | 5 × 5 |
| random3Sat | n=30 | 0.306 | ± 0.008 | 2.5% | 0.267 | 0.339 | 5 × 5 |
| random3Sat | n=60 | 3.740 | ± 0.055 | 1.5% | 3.593 | 3.901 | 5 × 5 |
| random5Sat | n=30 | 2.316 | ± 0.034 | 1.5% | 2.211 | 2.454 | 5 × 5 |
| random5Sat | n=60 | 65.031 | ± 1.301 | 2.0% | 60.847 | 74.427 | 5 × 5 |

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — unoptimized baseline ([json](results/2026-08-28-laraserver4.json))
- [2026-08-28, laraserver4, 5-SAT](results/2026-08-28-laraserver4-5sat.md) — occurrence lists, with the `random5Sat` workload; reference for the optimizations ([json](results/2026-08-28-laraserver4-5sat.json))
- [2026-08-28, laraserver4, 2WL](results/2026-08-28-laraserver4-2wl.md) — two watched literals; 1.7–4.6× on the larger instances ([json](results/2026-08-28-laraserver4-2wl.json))
