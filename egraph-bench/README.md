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

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call. Append a regex to run a subset, e.g. `... "egraphBench/Jmh/run .*star.*"`.

`jmh2md.py` prints a Markdown table with a 95% confidence interval computed from the raw per-iteration samples of all forks (JMH's own `Error` column is a 99.9% interval). Its only dependency, `scipy`, is listed in [requirements.txt](requirements.txt).

> [!WARNING]
> Do not chain `shutdown` after `Jmh/run` in the same sbt command string (`sbt "egraphBench/Jmh/run; shutdown"`): with sbt 2's thin client the forked JMH process is still starting when the server shuts down and deletes the classpath copy under `target/bg-jobs/`, so JMH fails with `NoSuchFileException ... META-INF/BenchmarkList`.

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-29. Setup and noise: [results/2026-08-29-laraserver4.md](results/2026-08-29-laraserver4.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.287 | ± 0.008 | 2.9% | 0.272 | 0.368 | 5 × 5 |
| backtrack | n=4000 | 1.504 | ± 0.035 | 2.4% | 1.424 | 1.774 | 5 × 5 |
| build | n=1000 | 0.178 | ± 0.002 | 1.1% | 0.171 | 0.189 | 5 × 5 |
| build | n=4000 | 1.021 | ± 0.100 | 9.8% | 0.877 | 2.059 | 5 × 5 |
| chain | n=1000 | 0.121 | ± 0.003 | 2.1% | 0.116 | 0.142 | 5 × 5 |
| chain | n=4000 | 0.479 | ± 0.004 | 0.9% | 0.461 | 0.502 | 5 × 5 |
| explain | n=1000 | 0.243 | ± 0.003 | 1.4% | 0.231 | 0.268 | 5 × 5 |
| explain | n=4000 | 1.290 | ± 0.016 | 1.2% | 1.248 | 1.382 | 5 × 5 |
| randomBatch | n=1000 | 0.226 | ± 0.002 | 1.0% | 0.219 | 0.246 | 5 × 5 |
| randomBatch | n=4000 | 1.254 | ± 0.012 | 1.0% | 1.227 | 1.327 | 5 × 5 |
| star | n=1000 | 0.109 | ± 0.002 | 1.4% | 0.103 | 0.116 | 5 × 5 |
| star | n=4000 | 0.435 | ± 0.006 | 1.3% | 0.415 | 0.459 | 5 × 5 |

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:

- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — baseline before any optimization ([json](results/2026-08-28-laraserver4.json))
- [2026-08-28, laraserver4, dense arrays](results/2026-08-28-laraserver4-arrays.md) — dense arrays, in-place canonicalization, smaller-class rescans ([json](results/2026-08-28-laraserver4-arrays.json))
- [2026-08-28, laraserver4, case class](results/2026-08-28-laraserver4-case-class.md) — same structures, `ENode` as a private case class with structural equality ([json](results/2026-08-28-laraserver4-case-class.json))
- [2026-08-28, laraserver4, Scala HashMap](results/2026-08-28-laraserver4-scala-hashmap.md) — hashcons as Scala `mutable.HashMap` instead of `java.util.HashMap`; about 5% slower on `randomBatch`, equal elsewhere; not adopted ([json](results/2026-08-28-laraserver4-scala-hashmap.json))
- [2026-08-28, laraserver4, backtracking](results/2026-08-28-laraserver4-backtrack.md) — `push`/`pop` with an undo trail, new `backtrack` workload ([json](results/2026-08-28-laraserver4-backtrack.json))
- [2026-08-28, laraserver4, proof production](results/2026-08-28-laraserver4-explain.md) — original arguments kept, justified congruences, union-tree explanations; new `explain` workload ([json](results/2026-08-28-laraserver4-explain.json))
- [2026-08-28, laraserver4, simplified explain](results/2026-08-28-laraserver4-explain-simplified.md) — `explain` rewritten for readability; no measurable difference ([json](results/2026-08-28-laraserver4-explain-simplified.json))
- [2026-08-28, laraserver4, simplifications](results/2026-08-28-laraserver4-simplified.md) — readability pass (`insertOrMerge`, no edge flip, no undo sentinels); no regression ([json](results/2026-08-28-laraserver4-simplified.json))
- [2026-08-28, laraserver4, while loops](results/2026-08-28-laraserver4-while.md) — `while` instead of boxing `for` loops over argument arrays in `add`; 3–10% ([json](results/2026-08-28-laraserver4-while.json))
- [2026-08-28, laraserver4, cached hash](results/2026-08-28-laraserver4-cached-hash.md) — hash of the canonical form cached in the node; no gain, not adopted ([json](results/2026-08-28-laraserver4-cached-hash.json))
- [2026-08-28, laraserver4, flat edges](results/2026-08-28-laraserver4-flat-edges.md) — union tree as parallel arrays instead of `Edge` objects; slower, not adopted ([json](results/2026-08-28-laraserver4-flat-edges.json))
- [2026-08-29, laraserver4](results/2026-08-29-laraserver4.md) — With the event log of merges and disequalities for theory propagation and the linear lowest common ancestor in `explain`; compared with the last record (proof production, `while` loops in `add`)
