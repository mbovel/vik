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

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, case-class `ENode` over dense arrays. Setup, noise and warmup analysis: [results/2026-08-28-laraserver4-case-class.md](results/2026-08-28-laraserver4-case-class.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| build | n=1000 | 0.154 | ± 0.015 | 9.6% | 0.134 | 0.280 | 5 × 5 |
| build | n=4000 | 0.882 | ± 0.044 | 5.0% | 0.810 | 1.258 | 5 × 5 |
| chain | n=1000 | 0.093 | ± 0.004 | 4.4% | 0.083 | 0.115 | 5 × 5 |
| chain | n=4000 | 0.394 | ± 0.030 | 7.7% | 0.348 | 0.621 | 5 × 5 |
| randomBatch | n=1000 | 0.191 | ± 0.002 | 0.8% | 0.185 | 0.198 | 5 × 5 |
| randomBatch | n=4000 | 1.178 | ± 0.023 | 1.9% | 1.113 | 1.371 | 5 × 5 |
| star | n=1000 | 0.075 | ± 0.001 | 1.3% | 0.072 | 0.080 | 5 × 5 |
| star | n=4000 | 0.345 | ± 0.038 | 10.9% | 0.300 | 0.732 | 5 × 5 |

All workloads are linear in `n`. Versus the [baseline](results/2026-08-28-laraserver4.md): 2.6–8.2× on `build`, `chain` and `randomBatch`, and `star` went from quadratic (3.3 s at n=4000) to 0.35 ms. Equivalent to the [dense-array run](results/2026-08-28-laraserver4-arrays.md) up to run-to-run noise.

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:

- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — baseline before any optimization ([json](results/2026-08-28-laraserver4.json))
- [2026-08-28, laraserver4, dense arrays](results/2026-08-28-laraserver4-arrays.md) — dense arrays, in-place canonicalization, smaller-class rescans ([json](results/2026-08-28-laraserver4-arrays.json))
- [2026-08-28, laraserver4, case class](results/2026-08-28-laraserver4-case-class.md) — same structures, `ENode` as a private case class with structural equality ([json](results/2026-08-28-laraserver4-case-class.json))
