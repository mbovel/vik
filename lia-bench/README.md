# LIA solver benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.lia.Solver`, in [SolverBench.scala](src/main/scala/net/bovel/lia/bench/SolverBench.scala).

## Workloads

Ten random systems per size `n` (10 or 20 variables in the box `[-10, 10]`): `2n` expressions of two or three terms with coefficients in `[-3, 3]`, each bounded on one side by a constant in `[-10, 10]`, a mix of feasible and infeasible systems. Each operation builds fresh solvers.

| Benchmark | Workload | Exercises |
|---|---|---|
| `solve` | asserts all bounds of a system, then one `check` | the simplex from scratch, and branch and bound |
| `incremental` | asserts the bounds one at a time with a `check` after each, until infeasible | the incremental simplex (what a CDCL(T) solver does), with bounds only tightening |
| `backtrack` | asserts the first half, then tries each remaining bound alone between a `push` and a `pop` | backtracking: the tableau is kept, only bounds are undone |

## Running

From the repository root (see [egraph-bench/README.md](../egraph-bench/README.md) for the setup of `jmh2md.py`):

```sh
sbt "liaBench/Jmh/run -rf json -rff target/jmh-result.json"
egraph-bench/.venv/bin/python egraph-bench/jmh2md.py target/jmh-result.json
```

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-29. Setup and noise: [results/2026-08-29-laraserver4.md](results/2026-08-29-laraserver4.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=10 | 1.820 | ± 0.021 | 1.1% | 1.789 | 1.921 | 3 × 5 |
| backtrack | n=20 | 77.873 | ± 1.082 | 1.4% | 75.671 | 82.306 | 3 × 5 |
| incremental | n=10 | 2.343 | ± 0.063 | 2.7% | 2.238 | 2.614 | 3 × 5 |
| incremental | n=20 | 40.789 | ± 0.771 | 1.9% | 39.437 | 43.111 | 3 × 5 |
| solve | n=10 | 0.928 | ± 0.021 | 2.2% | 0.889 | 0.987 | 3 × 5 |
| solve | n=20 | 9.619 | ± 0.144 | 1.5% | 9.293 | 10.107 | 3 × 5 |

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-29, laraserver4, experiments](results/2026-08-29-laraserver4-experiments.md) — `BigInt` rationals, the `Long` fast path (kept), most-fractional branching (not kept); quick configuration
- [2026-08-29, laraserver4](results/2026-08-29-laraserver4.md) — First full run of `SolverBench`: simplex with branch and bound, `Long` fast-path rationals, before bound propagation
