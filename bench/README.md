# Benchmarks

One [JMH](https://github.com/openjdk/jmh) project for all the modules, in [src/main/scala/io/bovel/bench](src/main/scala/io/bovel/bench):
a package per module with its benchmark class, and `evaluation` for the comparison with other solvers on SMT-LIB
files. The workloads and the results are kept per module:

| Module | Benchmark class | Workloads and results |
|---|---|---|
| `egraph` | `egraph.EGraphBench` | [results/egraph](results/egraph/README.md) |
| `cdclt` | `cdclt.CdcltBench` | [results/cdclt](results/cdclt/README.md) |
| `lia` | `lia.LiaBench` | [results/lia](results/lia/README.md) |
| `smtlib` | `smtlib.SExprBench` (the reader), `smtlib.SmtlibBench` (the parser and the printer) | [results/sexpr](results/sexpr/README.md), [results/smtlib](results/smtlib/README.md) |
| `vik` (random integer and datatype instances through the API) | `vik.UfliaBench`, `vik.AdtBench` | [results/smt](results/smt/README.md) |
| `evaluation` (VIK, z3, cvc5, princess and SMTInterpol in process on the SMT-LIB instances of the evaluation) | `evaluation.VikBench`, `Z3Bench`, `Z3TextBench`, `Cvc5Bench`, `PrincessBench`, `SmtInterpolBench`; `SolverCheck`, `replaycheck` | [results/smt](results/smt/README.md) |

## The evaluation page

[evaluate.sh](evaluate.sh) reproduces the two tables of
[the evaluation](../docs/_docs/evaluation.md) end to end and rewrites the page in place. The warm table is one
JMH run of the suites of `io.bovel.bench.evaluation` — one per solver, `VikBench`, `Z3Bench` and `Z3TextBench`,
`Cvc5Bench`, `PrincessBench`, `SmtInterpolBench`, each listing the instances it takes and, commented, the ones
it does not and why:

```sh
bench/evaluate.sh all     # everything below, in order
bench/evaluate.sh warm    # the in-process JMH table       (the z3 and cvc5 bindings of fetch-native.sh)
bench/evaluate.sh cold    # the process-per-question table (the z3 and cvc5 binaries of fetch-native.sh)
bench/evaluate.sh regen   # only regenerate the page from the newest dated results (seconds)
```

Every single measurement holds to a ten-second budget. A warm operation past it is halted by a watchdog
([Budget.scala](src/main/scala/io/bovel/bench/evaluation/Budget.scala)) and noted on [timeouts.txt](timeouts.txt) —
committed, so the combination is refused on every later run and its cell reads 🐢. A solver that answers
neither `sat` nor `unsat` is noted the same way — reporting its `unknown` as `unsat` made a solver that
gave up look like one that decided — so the mark means "did not answer in ten seconds", either way; a fixed solver
earns its line's removal. A cold run is cut by `timeout 10` (in [cold.sh](cold.sh)) with the same cell.
The instances of both tables, in their order, are [evaluation-instances.txt](evaluation-instances.txt).

It checks its prerequisites first (the Python venv above, `bench/fetch-native.sh`, the cold binaries) and
fails with the command to run. Results land in `results/smt/` as `<date>-<host>-*.{json,tsv}`; `regen`
always reads the newest set, so committed results are the reference and a fresh machine reproduces the
trends, not the exact milliseconds.

## Running

From the repository root:

```sh
python3 -m venv bench/.venv && bench/.venv/bin/pip install -r bench/requirements.txt   # once, for jmh2md.py
bench/fetch-native.sh                                                 # once, the native libraries of z3 and cvc5 (Linux x86-64)
sbt "bench/Jmh/run -l"                                                # lists the benchmarks
sbt "bench/Jmh/run .*EGraphBench.*"                                   # one module
sbt "bench/Jmh/run -rf json -rff target/jmh-result.json .*LiaBench.*" # with the JSON results
bench/.venv/bin/python bench/jmh2md.py target/jmh-result.json         # Markdown table with 95% confidence intervals
```

To sweep an arbitrary corpus subset one process per solver and instance, hand [cold.sh](cold.sh) a file
list and a timeout: `find corpus -name '*.smt2' | grep -E <regex> > /tmp/list && bench/cold.sh /tmp/list 1 60`.

Configuration (annotations in the benchmark classes): 3 forks, 5 warmup and 5 measurement iterations of 1 s each
for the modules' benchmarks, 3 forks, 3 and 3 for the solver suites, average time per operation, which gives 95%
confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call. Every run gets a `timeout`: an sbt client can block on an unresponsive server (see
[external-issues.md](../external-issues.md)).

`jmh2md.py` prints a Markdown table with a 95% confidence interval computed from the raw per-iteration samples of all
forks (JMH's own `Error` column is a 99.9% interval). Its only dependency, `scipy`, is listed in
[requirements.txt](requirements.txt).

> [!WARNING]
> Do not chain `shutdown` after `Jmh/run` in the same sbt command string: with sbt 2's thin client the forked JMH
> process is still starting when the server shuts down and deletes the classpath copy under `target/bg-jobs/`.

## Results

One file per run under `results/<module>/`, named by date and host (plus a tag when a day has several runs), with the
raw JMH JSON next to it; each module's README holds its current results and the list of all runs.
