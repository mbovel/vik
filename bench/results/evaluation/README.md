# The history of the evaluation

Every run of [bench/evaluate.sh](../../evaluate.sh) leaves its measurements here, and the two tables of
[the evaluation](../../../docs/_docs/evaluation.md) are built from them. For each table it measures, a run
writes a CSV file of the measurements, a row per solver and instance, and a JSON file of the same name that
says what was measured: one such pair for VIK, one for the other solvers. None is changed afterwards.

## Names

`<run>-<host>-<solvers>-<table>`, as in `2026-11-11T0200Z-laraserver4-vik-warm.csv`:

| Part | Meaning |
|---|---|
| `run` | The minute `evaluate.sh` started, in UTC. The files of one invocation share it, and the names sort by it. |
| `host` | The machine. Times of two machines do not compare. |
| `solvers` | `vik` for VIK, `others` for z3, cvc5, princess and SMTInterpol. |
| `table` | `warm` for the JMH run of the suites, `cold` for the run of [cold.sh](../../cold.sh), a process per instance. |

VIK and the other solvers are two series of files because they are measured separately: the others are
pinned, so `bench/evaluate.sh vik` after a change to VIK adds to VIK's series only, and `bench/evaluate.sh
others` is due when a version, a replay or the list of instances changes. `bench/evaluate.sh all` adds to
both series, under the same `run`.

## The CSV file

```
warm: solver,instance,samples_ms
cold: solver,instance,samples_s
```

| Column | Meaning |
|---|---|
| `solver` | Warm: the JMH suite and its method (`VikBench.api`, `VikBench.text`, `Z3Bench.api`, `Z3TextBench.text`, `Cvc5Bench.api`, `Cvc5Bench.text`, `PrincessBench.api`, `SmtInterpolBench.api`). Cold: the name `cold.sh` gives the command (`vik-jvm`, `vik-jvm-aot`, `vik-node`, `vik-native`, `vik-graal`, `z3`, `cvc5`, `princess`, `smtinterpol`). |
| `instance` | The name of the corpus file, without `.smt2`. |
| `samples_ms` | Warm: JMH's iterations in milliseconds per operation, separated by spaces, fork after fork, to four significant digits. |
| `samples_s` | Cold: the wall-clock seconds of each repetition, separated by spaces. |

The samples are in the unit of the table they feed, and the files hold no statistics: the tables compute
the mean and its 95% confidence interval (warm) and the median (cold) when they are built.

A row holds a word in place of its samples when there is no time to show:

| Word | Meaning | On the page |
|---|---|---|
| `timeout` | Warm: the combination is on [timeouts.txt](../../timeouts.txt), past the ten-second budget, and was not run. Cold: a repetition was cut at the budget. | 🐢 |
| `error` | Cold: a repetition did not answer every `check-sat`, as on a file the solver rejects. | — |
| `unknown` | Cold: a repetition answered `unknown` where the status of the file says `sat` or `unsat`. | **unknown** |
| `wrong` | Cold: a repetition answered `sat` where the status says `unsat`, or the reverse. | **wrong** |

The answers themselves are not kept: they are checked when the run is recorded. `cold.sh` reads what
every repetition prints, and [history.py](../../history.py) compares it with the statuses of the corpus
file, one per `check-sat`. A row has samples only if every repetition answered every check as its status
says, a check whose status is unknown accepting any answer; a wrong answer is also named in the output of
the run. A JMH run records times and nothing else: the answers of the solvers in process are compared
with the statuses by a tool of its own, `SolverCheck` ([the checkers](../../README.md#the-checkers)).

## The JSON file

What is the same for every row of the CSV file beside it, here for the other solvers of a cold run:

```json
{
  "commit": "32e844fc",
  "jdk": "25.0.5",
  "versions": {
    "z3": "4.14.0",
    "cvc5": "1.3.3",
    "princess": "2026-05-20",
    "smtinterpol": "2.5-1388-ga5a4ab0c"
  }
}
```

| Key | Meaning |
|---|---|
| `commit` | The commit of this repository that was measured, which is VIK's version; `-dirty` follows it when the tree differed from the commit in more than results and pages. |
| `jdk` | The JDK that ran the JVM solvers. |
| `versions` | In the files of the other solvers only. For each solver of the CSV file, under its name there: the version of the JVM library (princess, SMTInterpol), of the JavaSMT binding (z3 and cvc5, warm) or of the binary (z3 and cvc5, cold). |

## From the files to the page

[The evaluation](../../../docs/_docs/evaluation.md) has a warm table and a cold table, a row per instance
and a column per solver, built by [warm2md.py](../../warm2md.py) from the warm files and by
[cold2md.py](../../cold2md.py) from the cold ones. What a table says about an instance next to its name
(its theory, whether it is incremental, its status) is not in the files: it is read from the corpus file
when the table is built.

The chart of the page is these two tables, drawn in the reader's browser
([evaluation.js](../../../docs/_assets/js/evaluation.js)): it reads the rows, the columns and the times off
them, and nothing else. For it, a cell with a time also holds the time in seconds whatever the unit of its
table, to the four digits of the files, as `data-seconds`; the tables are found by their ids, `warm-times`
and `cold-times`.

On the host of the newest run, each solver's column is that solver's own newest run, whole
(`latest` in [history.py](../../history.py)); the caption of the table names the version and the time of
each, and links the files. A solver that was not measured on an instance in its newest run has no
number there, whatever an older run measured.

The runs made before these files existed, up to September 2026, are in [../smt](../smt/README.md): a JMH
JSON file and a TSV file per day, every solver in each.
