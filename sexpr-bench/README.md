# S-expression parser benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for `net.bovel.sexpr.SExprParser`, in [SExprBench.scala](src/main/scala/net/bovel/sexpr/bench/SExprBench.scala).

## Workloads

| Benchmark | Workload | Exercises |
|---|---|---|
| `parse` | a generated text of `n` (1000 or 10000) S-expressions, nested applications of depth 3 over symbols, numerals, quoted symbols and keywords, with a comment every ten expressions | the regex lexer and the tree construction |

## Running

From the repository root (see [egraph-bench/README.md](../egraph-bench/README.md) for the setup of `jmh2md.py`):

```sh
sbt "sexprBench/Jmh/run -rf json -rff target/jmh-result.json"
egraph-bench/.venv/bin/python egraph-bench/jmh2md.py target/jmh-result.json
```

Configuration: 5 forks, 10 warmup and 5 measurement iterations of 1 s each, average time per operation.

## Current results

(none yet)

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
