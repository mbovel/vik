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

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, with two watched literals, VMTF, phase saving and Luby restarts. Setup and noise: [results/2026-08-28-laraserver4-restarts.md](results/2026-08-28-laraserver4-restarts.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| pigeonhole | n=30 | 0.001 | ± 0.000 | 3.6% | 0.001 | 0.001 | 5 × 5 |
| pigeonhole | n=60 | 1.673 | ± 0.031 | 1.8% | 1.536 | 1.811 | 5 × 5 |
| random3Sat | n=30 | 0.318 | ± 0.005 | 1.7% | 0.299 | 0.336 | 5 × 5 |
| random3Sat | n=60 | 2.344 | ± 0.032 | 1.4% | 2.228 | 2.457 | 5 × 5 |
| random5Sat | n=30 | 2.146 | ± 0.035 | 1.6% | 2.047 | 2.305 | 5 × 5 |
| random5Sat | n=60 | 23.374 | ± 0.404 | 1.7% | 21.604 | 25.372 | 5 × 5 |

Versus the [2WL run](results/2026-08-28-laraserver4-2wl.md): 1.6× on `random3Sat` n=60 and 2.8× on `random5Sat` n=60, but 0.14× on `pigeonhole` n=60, whose static order suits the resolution refutation.

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — unoptimized baseline ([json](results/2026-08-28-laraserver4.json))
- [2026-08-28, laraserver4, 5-SAT](results/2026-08-28-laraserver4-5sat.md) — occurrence lists, with the `random5Sat` workload; reference for the optimizations ([json](results/2026-08-28-laraserver4-5sat.json))
- [2026-08-28, laraserver4, 2WL](results/2026-08-28-laraserver4-2wl.md) — two watched literals; 1.7–4.6× on the larger instances ([json](results/2026-08-28-laraserver4-2wl.json))
- [2026-08-28, laraserver4, VMTF](results/2026-08-28-laraserver4-vmtf.md) — variable move-to-front alone: 1.55× on 3-SAT n=60, 0.33× on pigeonhole n=60 ([json](results/2026-08-28-laraserver4-vmtf.json))
- [2026-08-28, laraserver4, phase saving](results/2026-08-28-laraserver4-phase.md) — VMTF + phase saving: 1.12–1.31× on 5-SAT over VMTF alone ([json](results/2026-08-28-laraserver4-phase.json))
- [2026-08-28, laraserver4, restarts](results/2026-08-28-laraserver4-restarts.md) — Luby restarts on top of VMTF and phase saving: 2.3× on 5-SAT n=60; the package vs 2WL ([json](results/2026-08-28-laraserver4-restarts.json))
