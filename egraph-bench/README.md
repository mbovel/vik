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

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-28, commit `48386fd`, before any optimization. Setup, noise and warmup analysis: [results/2026-08-28-laraserver4.md](results/2026-08-28-laraserver4.md).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| build | n=1000 | 0.436 | ± 0.021 | 4.8% | 0.374 | 0.574 | 5 × 5 |
| build | n=4000 | 2.312 | ± 0.117 | 5.1% | 2.052 | 3.411 | 5 × 5 |
| chain | n=1000 | 0.711 | ± 0.030 | 4.3% | 0.639 | 0.893 | 5 × 5 |
| chain | n=4000 | 3.237 | ± 0.178 | 5.5% | 2.735 | 4.101 | 5 × 5 |
| randomBatch | n=1000 | 0.779 | ± 0.018 | 2.3% | 0.717 | 0.879 | 5 × 5 |
| randomBatch | n=4000 | 3.972 | ± 0.115 | 2.9% | 3.639 | 4.664 | 5 × 5 |
| star | n=1000 | 171.087 | ± 5.994 | 3.5% | 151.024 | 202.504 | 5 × 5 |
| star | n=4000 | 3266.638 | ± 50.031 | 1.5% | 3077.132 | 3527.469 | 5 × 5 |

`build`, `chain` and `randomBatch` scale linearly; `star` is quadratic (×19 for ×4 in `n`): at n=4000 each incremental merge costs ≈ 0.8 ms, versus ≈ 0.8 µs in `chain`.

## All results

One file per run, named by date and host, with the raw JMH JSON next to it:

- [2026-08-28, laraserver4](results/2026-08-28-laraserver4.md) — baseline before any optimization ([json](results/2026-08-28-laraserver4.json))
