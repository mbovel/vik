# E-graph benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.egraph.EGraph`, in [EGraphBench.scala](src/main/scala/net/bovel/egraph/bench/EGraphBench.scala).

## Workloads

Each benchmark builds a fresh e-graph, so its time includes construction; `build` isolates that part. `n` is 1000 or 4000.

| Benchmark | Workload | Exercises |
|---|---|---|
| `build` | `n` symbol classes, then `2n` random binary nodes `h(x, y)` over them | `add` (hashcons) |
| `randomBatch` | `build`, then `n / 2` random merges and a single `rebuild` | batch congruence closure (egg-style) |
| `chain` | leaves `x_i` with parents `f(x_i)`; merge `x_i = x_{i+1}` and `rebuild` after each | incremental use (CDCL(T)-style) |
| `star` | class `a` with `n` parents `g(a, c_j)`; merge `a` with `n` fresh singletons, `rebuild` after each | whether `rebuild` rescans the parents of the larger class on every merge (quadratic) or only those of the smaller one (linear) |
| `backtrack` | `build`, then `n / 2` times: `push`, one random merge, `rebuild`, `pop` | the undo trail: cost of a merge + rebuild + its undo |
| `explain` | `randomBatch` with distinct reasons, then `explain` for every pair of consecutive equal ids | explanations (union-tree walks, congruence recursion) |

## Running

From the repository root:

```sh
python3 -m venv egraph-bench/.venv && egraph-bench/.venv/bin/pip install -r egraph-bench/requirements.txt   # once
sbt "egraphBench/Jmh/run -rf json -rff target/jmh-result.json"
egraph-bench/.venv/bin/python egraph-bench/jmh2md.py target/jmh-result.json
```

Configuration (annotations in the benchmark class): 5 forks, 10 warmup and 5 measurement iterations of 1 s each, average time per operation. A full run takes about 15 minutes. Append a regex to run a subset, e.g. `... "egraphBench/Jmh/run .*star.*"`.

`jmh2md.py` prints a Markdown table with a 95% confidence interval computed from the raw per-iteration samples of all forks (JMH's own `Error` column is a 99.9% interval). Its only dependency, `scipy`, is listed in [requirements.txt](requirements.txt).

> [!WARNING]
> Do not chain `shutdown` after `Jmh/run` in the same sbt command string (`sbt "egraphBench/Jmh/run; shutdown"`): with sbt 2's thin client the forked JMH process is still starting when the server shuts down and deletes the classpath copy under `target/bg-jobs/`, so JMH fails with `NoSuchFileException ... META-INF/BenchmarkList`.

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, `EGraph` with proof production. Setup, noise and warmup analysis: [results/2026-08-28-laraserver4-explain.md](results/2026-08-28-laraserver4-explain.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.310 | ± 0.023 | 7.6% | 0.273 | 0.546 | 5 × 5 |
| backtrack | n=4000 | 1.485 | ± 0.027 | 1.8% | 1.438 | 1.734 | 5 × 5 |
| build | n=1000 | 0.190 | ± 0.015 | 8.1% | 0.175 | 0.350 | 5 × 5 |
| build | n=4000 | 1.019 | ± 0.057 | 5.6% | 0.935 | 1.489 | 5 × 5 |
| chain | n=1000 | 0.119 | ± 0.001 | 0.6% | 0.116 | 0.122 | 5 × 5 |
| chain | n=4000 | 0.478 | ± 0.003 | 0.6% | 0.469 | 0.491 | 5 × 5 |
| explain | n=1000 | 0.265 | ± 0.002 | 0.8% | 0.258 | 0.286 | 5 × 5 |
| explain | n=4000 | 1.427 | ± 0.018 | 1.3% | 1.370 | 1.521 | 5 × 5 |
| randomBatch | n=1000 | 0.247 | ± 0.004 | 1.5% | 0.236 | 0.269 | 5 × 5 |
| randomBatch | n=4000 | 1.328 | ± 0.021 | 1.6% | 1.244 | 1.404 | 5 × 5 |
| star | n=1000 | 0.108 | ± 0.002 | 1.6% | 0.100 | 0.115 | 5 × 5 |
| star | n=4000 | 0.437 | ± 0.007 | 1.6% | 0.420 | 0.473 | 5 × 5 |

All workloads are linear in `n`. Proof production costs 12–29% on the workloads that do not ask for explanations (see the results file); `explain` adds 0.02–0.10 ms to `randomBatch`.

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:

- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — baseline before any optimization ([json](results/2026-08-28-laraserver4.json))
- [2026-08-28, laraserver4, dense arrays](results/2026-08-28-laraserver4-arrays.md) — dense arrays, in-place canonicalization, smaller-class rescans ([json](results/2026-08-28-laraserver4-arrays.json))
- [2026-08-28, laraserver4, case class](results/2026-08-28-laraserver4-case-class.md) — same structures, `ENode` as a private case class with structural equality ([json](results/2026-08-28-laraserver4-case-class.json))
- [2026-08-28, laraserver4, Scala HashMap](results/2026-08-28-laraserver4-scala-hashmap.md) — hashcons as Scala `mutable.HashMap` instead of `java.util.HashMap`; about 5% slower on `randomBatch`, equal elsewhere; not adopted ([json](results/2026-08-28-laraserver4-scala-hashmap.json))
- [2026-08-28, laraserver4, backtracking](results/2026-08-28-laraserver4-backtrack.md) — `push`/`pop` with an undo trail, new `backtrack` workload ([json](results/2026-08-28-laraserver4-backtrack.json))
- [2026-08-28, laraserver4, proof production](results/2026-08-28-laraserver4-explain.md) — original arguments kept, justified congruences, union-tree explanations; new `explain` workload ([json](results/2026-08-28-laraserver4-explain.json))
