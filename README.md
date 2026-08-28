# The Verification Intermediate Kit (VIK)

VIK is a minimal but reasonnable SMT solver for the theories of linear integer arithmetic (LIA), algebraic data types (ADTs) and uninterpreted functions (UFs). It is designed to be used as a backend for verification tools, and to be easily understandable and modifiable.

## Design Principle

VIK is meant to be a minimal: its codbase is small, modular, and easy to understand, with a focus on clarity over performance. It should also be verification-friendly; verifying parts of VIK is future work.

While not optimized for performance, its performance should be within an order of magnitude of state-of-the-art solvers such as Z3 or CVC5.

## Components

- EGraph: a standalone e-graph implementation.
- LIA Solver: a simple solver for linear integer arithmetic.
- CDLC(T) Solver: a SAT modulo theory solver, with pluggable theory solvers for LIA, ADTs and UFs.

## Requirements

- A JDK (17 or later)
- [sbt](https://www.scala-sbt.org/) 2
- [Node.js](https://nodejs.org/), for the Scala.js tests and the playground

## Development

Subprojects: `egraph` (JVM), `egraphJS` (Scala.js, same sources) and `egraphPlayground` (a browser playground for the e-graph).

```sh
sbt compile   # compiles all subprojects
sbt test      # runs the tests on the JVM and on Scala.js
```

> [!NOTE]
> sbt 2 caches task results by content: when nothing changed since the last run, `sbt test` reports `Passed: Total 0` instead of re-running the tests, and `clean` does not reset this cache. To force a run, use `sbt "egraph/testOnly *"` or `sbt "egraphJS/testOnly *"`.

### Playground

Build it, serve its output directory, and open <http://localhost:8000/classes/index.html>:

```sh
sbt "egraphPlayground/copyResources; egraphPlayground/fastLinkJS"
python3 -m http.server --directory target/out/sjs1/scala-3.9.0/egraph-playground 8000
```
