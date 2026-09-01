# Benchmarks

One [JMH](https://github.com/openjdk/jmh) project for all the modules, in [src/main/scala/net/bovel/bench](src/main/scala/net/bovel/bench):
one benchmark class per module, plus the comparison with other solvers on SMT-LIB files. The workloads and the results
are kept per module:

| Module | Benchmark class | Workloads and results |
|---|---|---|
| `egraph` | `EGraphBench` | [results/egraph](results/egraph/README.md) |
| `cdclt` | `CdcltBench` | [results/cdclt](results/cdclt/README.md) |
| `lia` | `LiaBench` | [results/lia](results/lia/README.md) |
| `sexpr` | `SExprBench` | [results/sexpr](results/sexpr/README.md) |
| `smt2` | `Smt2Bench` | [results/smt2](results/smt2/README.md) |
| `vik` (SMT-LIB instances; princess, SMTInterpol, z3 and cvc5 in process; datatypes) | `SmtBench`, `OtherSolversBench`, `AdtBench`, `SolverCheck` | [results/smt](results/smt/README.md) |

## The evaluation page

[evaluate.sh](evaluate.sh) reproduces the two tables of
[the evaluation](../docs/_docs/evaluation.md) end to end and rewrites the page in place — the instance
groups, the per-solver measurement budgets and the deliberately blank cells all live in that one script:

```sh
bench/evaluate.sh all     # everything below, in order
bench/evaluate.sh warm    # the in-process JMH table       (z3 and cvc5 native libraries)
bench/evaluate.sh cold    # the process-per-question table (z3 and cvc5 on the PATH)
bench/evaluate.sh regen   # only regenerate the page from the newest dated results (seconds)
```

Every single measurement holds to a five-second budget. A warm operation past it is halted by a watchdog
([Budget.scala](src/main/scala/net/bovel/bench/Budget.scala)) and noted on [timeouts.txt](timeouts.txt) —
committed, so the combination is refused on every later run and its cell reads `timeout`; a fixed solver
earns its line's removal. A cold run is cut by `timeout 5` (in [cold.sh](cold.sh)) with the same cell.

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
bench/compare.sh 60 > bench/results/smt/compare.tsv                   # SMT-LIB files: one process per solver and instance
```

Configuration (annotations in the benchmark classes): 3 forks, 5 warmup and 5 measurement iterations of 1 s each,
average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line
for a closer call. Every run gets a `timeout`: an sbt client can block on an unresponsive server (see
[external-issues.md](../external-issues.md)).

`jmh2md.py` prints a Markdown table with a 95% confidence interval computed from the raw per-iteration samples of all
forks (JMH's own `Error` column is a 99.9% interval). Its only dependency, `scipy`, is listed in
[requirements.txt](requirements.txt).

`compare.sh` runs each solver as a separate process with a timeout (default 60 s) and records the answer and the wall
clock, JVM start-up included for VIK, princess and SMTInterpol; an optional second argument is a regex selecting files.
z3 and cvc5 are taken from the `PATH`; princess and SMTInterpol are fetched by sbt.

> [!WARNING]
> Do not chain `shutdown` after `Jmh/run` in the same sbt command string: with sbt 2's thin client the forked JMH
> process is still starting when the server shuts down and deletes the classpath copy under `target/bg-jobs/`.

## Results

One file per run under `results/<module>/`, named by date and host (plus a tag when a day has several runs), with the
raw JMH JSON next to it; each module's README holds its current results and the list of all runs.
