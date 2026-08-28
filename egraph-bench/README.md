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

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, dense-array `EGraph`. Setup, noise, warmup analysis and comparison with the baseline: [results/2026-08-28-laraserver4-arrays.md](results/2026-08-28-laraserver4-arrays.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| build | n=1000 | 0.165 | ± 0.009 | 5.2% | 0.143 | 0.227 | 5 × 5 |
| build | n=4000 | 1.014 | ± 0.036 | 3.6% | 0.860 | 1.164 | 5 × 5 |
| chain | n=1000 | 0.114 | ± 0.009 | 7.7% | 0.092 | 0.171 | 5 × 5 |
| chain | n=4000 | 0.459 | ± 0.035 | 7.7% | 0.371 | 0.756 | 5 × 5 |
| randomBatch | n=1000 | 0.205 | ± 0.006 | 2.8% | 0.194 | 0.249 | 5 × 5 |
| randomBatch | n=4000 | 1.209 | ± 0.019 | 1.5% | 1.121 | 1.286 | 5 × 5 |
| star | n=1000 | 0.080 | ± 0.001 | 1.1% | 0.078 | 0.084 | 5 × 5 |
| star | n=4000 | 0.415 | ± 0.056 | 13.4% | 0.319 | 0.829 | 5 × 5 |

All workloads are linear in `n`. Versus the [baseline](results/2026-08-28-laraserver4.md): 2.3–7× on `build`, `chain` and `randomBatch`, and `star` went from quadratic (3.3 s at n=4000) to 0.4 ms.

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:

- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — baseline before any optimization ([json](results/2026-08-28-laraserver4.json))
- [2026-08-28, laraserver4, dense arrays](results/2026-08-28-laraserver4-arrays.md) — dense arrays, in-place canonicalization, smaller-class rescans ([json](results/2026-08-28-laraserver4-arrays.json))
