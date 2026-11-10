# The history of the evaluation

Every run of [bench/evaluate.sh](../../evaluate.sh) appends its measurements to four CSV files here, and
the two tables of [the evaluation](../../../docs/_docs/evaluation.md) are built from them. A row is one
solver on one instance in one run.

| File | Solvers |
|---|---|
| `vik-warm.csv` | `VikBench.api`, `VikBench.text` |
| `others-warm.csv` | `Z3Bench.api`, `Z3TextBench.text`, `Cvc5Bench.api`, `Cvc5Bench.text`, `PrincessBench.api`, `SmtInterpolBench.api` |
| `vik-cold.csv` | `vik-jvm`, `vik-jvm-aot`, `vik-node`, `vik-native`, `vik-graal` |
| `others-cold.csv` | `z3`, `cvc5`, `princess`, `smtinterpol` |

VIK and the other solvers are in separate files because they are measured separately: the others are
pinned, so `bench/evaluate.sh vik` after a change to VIK leaves their rows alone, and `bench/evaluate.sh
others` adds to them when a version, a replay or the list of instances changes.

## Columns

The first six columns say what was measured and on what, and the four files have them. The columns after
them are the measurement, and the warm files and the cold files differ there:

```
vik-warm.csv, others-warm.csv: run,host,jdk,solver,version,instance,samples_ms
vik-cold.csv, others-cold.csv: run,host,jdk,solver,version,instance,expected,answer,samples_s
```

| Column | Files | Meaning |
|---|---|---|
| `run` | all four | The minute `evaluate.sh` started; every row of the invocation has it. |
| `host` | all four | The machine. Times of two machines do not compare. |
| `jdk` | all four | The JDK that ran the JVM solvers. |
| `solver` | all four | Warm: the JMH suite and its method. Cold: the name `cold.sh` gives the command. |
| `version` | all four | VIK: the commit, with `-dirty` when the tree differed from it in more than results and pages. Others, warm: the version of the JVM library or of the JavaSMT binding. Others, cold: the version of the binary (z3, cvc5) or of the JVM library. |
| `instance` | all four | The name of the corpus file, without `.smt2`. |
| `samples_ms` | warm | JMH's iterations in milliseconds per operation, separated by spaces, fork after fork, to four significant digits. Empty for a combination on [timeouts.txt](../../timeouts.txt): past the ten-second budget, not run. |
| `expected` | cold | The statuses of the file, one per `check-sat`. |
| `answer` | cold | The solver's answers, one per `check-sat`; `timeout` for a run cut at the budget, `error` for one that did not answer every check. |
| `samples_s` | cold | The wall-clock seconds of each repetition, separated by spaces. |

Only the cold files have `expected` and `answer`, because only a cold run sees an answer: `cold.sh` starts
the solver as a process and reads what it prints, and the answer decides what the page shows for the run
(🐢 for a `timeout`, `—` for an `error`, the answer in bold when it is not the expected one). A JMH run
records times and nothing else; the answers of the solvers in process are compared with the statuses by a
tool of its own, `SolverCheck` ([the checkers](../../README.md#the-checkers)). The samples are in the unit
of the table they feed: milliseconds for the warm one, seconds for the cold one.

The files hold samples, not statistics: the tables of the page compute the mean and its 95% confidence
interval (warm) and the median (cold) when they are built.

## From the files to the page

[The evaluation](../../../docs/_docs/evaluation.md) has a warm table and a cold table, a row per instance
and a column per solver, built by [warm2md.py](../../warm2md.py) from the two warm files and by
[cold2md.py](../../cold2md.py) from the two cold files. What a table says about an instance next to its
name (its theory, whether it is incremental, its status) is not in the files: it is read from the corpus
file when the table is built, except the status of the cold table, which is `expected`.

On the host of the newest run, each solver's column is that solver's own newest run, whole
(`latest` in [history.py](../../history.py)); the caption of the table names the version and the time of
each. A solver that was not measured on an instance in its newest run has no number there, whatever an
older run measured.

The runs made before these files existed, up to September 2026, are in [../smt](../smt/README.md): a JMH
JSON file and a TSV file per day, every solver in each.
