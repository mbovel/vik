# LIA solver benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `io.bovel.lia.Solver`, in [LiaBench.scala](../../src/main/scala/io/bovel/bench/lia/LiaBench.scala).

## Workloads

Ten random systems per size `n` (10 or 20 variables in the box `[-10, 10]`): `2n` expressions of two or three terms with coefficients in `[-3, 3]`, each bounded on one side by a constant in `[-10, 10]`, a mix of feasible and infeasible systems; and ten structured chains (`stepChains`). Each operation builds fresh solvers.

| Benchmark | Workload | Exercises |
|---|---|---|
| `solve` | asserts all bounds of a system, then one `check` | the simplex from scratch, and branch and bound |
| `incremental` | asserts the bounds one at a time with a `check` after each, until infeasible | the incremental simplex (what a CDCL(T) solver does), with bounds only tightening |
| `backtrack` | asserts the first half, then tries each remaining bound alone between a `push` and a `pop` | backtracking: the tableau is kept, only bounds are undone |
| `stepChains` | ten chains `x_{i+1} = x_i + a_i × y_i` of `n` steps (`y_i` in `[0, 3]`, `a_i` in `[2, 5]`) from `x_0 = 0` to a window of width 1 on `x_n` | bound propagation along a chain, branch and bound where the relaxation is loose |

## Running

From the repository root (see [bench/README.md](../../README.md) for the setup of `jmh2md.py`):

```sh
sbt "bench/Jmh/run -rf json -rff target/jmh-result.json"
bench/.venv/bin/python bench/jmh2md.py target/jmh-result.json
```

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-29. Setup and noise: [results/2026-08-29-laraserver4-propagation.md](2026-08-29-laraserver4-propagation.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=10 | 9.728 | ± 0.108 | 1.1% | 9.546 | 10.130 | 3 × 5 |
| backtrack | n=20 | 102.247 | ± 1.681 | 1.6% | 99.062 | 108.563 | 3 × 5 |
| incremental | n=10 | 10.409 | ± 0.209 | 2.0% | 10.053 | 11.138 | 3 × 5 |
| incremental | n=20 | 164.960 | ± 4.212 | 2.6% | 160.005 | 184.588 | 3 × 5 |
| solve | n=10 | 1.584 | ± 0.035 | 2.2% | 1.517 | 1.700 | 3 × 5 |
| solve | n=20 | 4.088 | ± 0.069 | 1.7% | 3.955 | 4.306 | 3 × 5 |
| stepChains | n=10 | 0.814 | ± 0.017 | 2.1% | 0.783 | 0.865 | 3 × 5 |
| stepChains | n=20 | 3.846 | ± 0.237 | 6.2% | 3.479 | 4.642 | 3 × 5 |

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-29, laraserver4, experiments](2026-08-29-laraserver4-experiments.md) — `BigInt` rationals, the `Long` fast path (kept), most-fractional branching (not kept); quick configuration
- [2026-08-29, laraserver4](2026-08-29-laraserver4.md) — First full run of `SolverBench`: simplex with branch and bound, `Long` fast-path rationals, before bound propagation
- [2026-08-29, laraserver4, bound propagation](2026-08-29-laraserver4-propagation.md) — every row vs. incremental over a worklist (4 or 1 visits per row; the latter kept), on the random workloads, `stepChains` and the SMT-LIB instances; quick configuration
- [2026-08-29, laraserver4](2026-08-29-laraserver4-propagation.md) — Record on the final code of the day: incremental bound propagation (one row visit per propagation), branch and bound, `Long` fast-path rationals, including the new `stepChains` workload; compared with the record before propagation
- [2026-08-30, laraserver4, lazy reasons](2026-08-29-laraserver4-lazy-reasons.md) — a derived bound keeps its premise bounds and collects literals only at conflicts (kept): 1.2–3.2× on the four workloads
- [2026-09-02, laraserver4, sparse rows](2026-09-02-laraserver4-sparse-rows.md) — rows of the tableau as sorted arrays instead of hash maps (not kept): no measurable effect on `LiaBench`, the evaluation's LIA instances or the Scala 3 queries; the boxing the profile showed is around the tableau, not in it
- [2026-09-03, laraserver4, int arrays](2026-09-03-laraserver4-int-arrays.md) — the tableau on `Int` arrays: sparse rows, columns as arrays whose entries the rows link to (`Row.slots`, `Solver.leave`), a gcd-free path for integer rationals (kept): 1.06–1.39× on `LiaBench`, 1.11–1.16× on the LIA instances, 1.13× on the Scala 3 queries, 0.90× on the wide bit-vector instance
- [2026-09-03, laraserver4, no-bounds fast path](2026-09-03-laraserver4-no-bounds.md) — `Sat` at once when no bound is asserted, the values reset to zero (not kept): no effect, the compiler's queries carry bounds at level 0 and take the path once in sixty checks
- [2026-09-04, laraserver4, what a check-sat pays for](2026-09-04-laraserver4-per-check.md) — the bound propagation's per-call array, the scan of every atom for the implied ones, and the small-model box recomputed at every check (kept): the incremental instances 1.3× to 2×, the one-shot ones unchanged
