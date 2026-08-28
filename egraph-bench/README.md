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

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, `EGraph` with backtracking. Setup, noise and warmup analysis: [results/2026-08-28-laraserver4-backtrack.md](results/2026-08-28-laraserver4-backtrack.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| backtrack | n=1000 | 0.258 | ± 0.017 | 6.7% | 0.231 | 0.416 | 5 × 5 |
| backtrack | n=4000 | 1.441 | ± 0.135 | 9.4% | 1.286 | 2.960 | 5 × 5 |
| build | n=1000 | 0.143 | ± 0.002 | 1.7% | 0.137 | 0.162 | 5 × 5 |
| build | n=4000 | 0.821 | ± 0.006 | 0.7% | 0.798 | 0.863 | 5 × 5 |
| chain | n=1000 | 0.087 | ± 0.001 | 1.4% | 0.084 | 0.100 | 5 × 5 |
| chain | n=4000 | 0.347 | ± 0.002 | 0.6% | 0.338 | 0.360 | 5 × 5 |
| randomBatch | n=1000 | 0.192 | ± 0.002 | 0.8% | 0.186 | 0.199 | 5 × 5 |
| randomBatch | n=4000 | 1.163 | ± 0.016 | 1.4% | 1.102 | 1.218 | 5 × 5 |
| star | n=1000 | 0.077 | ± 0.001 | 1.4% | 0.074 | 0.083 | 5 × 5 |
| star | n=4000 | 0.321 | ± 0.003 | 1.0% | 0.313 | 0.337 | 5 × 5 |

All workloads are linear in `n`. Versus the [baseline](results/2026-08-28-laraserver4.md): 2.6–9× on `build`, `chain` and `randomBatch`, and `star` went from quadratic (3.3 s at n=4000) to 0.3 ms. The undo trail costs nothing at level 0 (see the comparison in the results file).

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:

- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — baseline before any optimization ([json](results/2026-08-28-laraserver4.json))
- [2026-08-28, laraserver4, dense arrays](results/2026-08-28-laraserver4-arrays.md) — dense arrays, in-place canonicalization, smaller-class rescans ([json](results/2026-08-28-laraserver4-arrays.json))
- [2026-08-28, laraserver4, case class](results/2026-08-28-laraserver4-case-class.md) — same structures, `ENode` as a private case class with structural equality ([json](results/2026-08-28-laraserver4-case-class.json))
- [2026-08-28, laraserver4, Scala HashMap](results/2026-08-28-laraserver4-scala-hashmap.md) — hashcons as Scala `mutable.HashMap` instead of `java.util.HashMap`; about 5% slower on `randomBatch`, equal elsewhere; not adopted ([json](results/2026-08-28-laraserver4-scala-hashmap.json))
- [2026-08-28, laraserver4, backtracking](results/2026-08-28-laraserver4-backtrack.md) — `push`/`pop` with an undo trail, new `backtrack` workload ([json](results/2026-08-28-laraserver4-backtrack.json))
