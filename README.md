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

### Benchmarks

The `egraphBench` subproject holds [JMH](https://github.com/openjdk/jmh) benchmarks for the e-graph:

```sh
sbt "egraphBench/Jmh/run"            # all benchmarks (about 15 minutes)
sbt "egraphBench/Jmh/run .*star.*"   # only the benchmarks matching a regex
```

See [egraph-bench/README.md](egraph-bench/README.md) for the workloads and the current results.

### Playground

Build it, serve its output directory, and open <http://localhost:8000/classes/index.html>:

```sh
sbt "egraphPlayground/copyResources; egraphPlayground/fastLinkJS"
python3 -m http.server --directory target/out/sjs1/scala-3.9.0/egraph-playground 8000
```

## References and Inspirations

- Nikolaj Bjørner, Clemens Eisenhofer, Arie Gurfinkel, Nuno P. Lopes, Leonardo de Moura, Lev Nachmanson and Christoph Wintersteiger. [*Z3 Internals (Draft)*](https://z3prover.github.io/papers/z3internals.html).
- Zachary DeVito. [*Intro to EGraphs*](https://colab.research.google.com/drive/1tNOQijJqe5tw-Pk9iqd6HHb2abC5aRid) (`egraphs_example.ipynb`): a Google Colab notebook with an executable Python prototype of the e-graph described in the [egg paper](https://egraphs-good.github.io).
- Sankalp Gambhir. [*smt-lia-pl-seminar*](https://github.com/sankalpgambhir/smt-lia-pl-seminar): building a small SAT solver with theory support from scratch. Talk given at the EPFL graduate PL seminar, 2025-05-13 and 2025-05-20.
- Matthieu Bovel and Solal Pirelli. [*paper-test-implementation*](https://github.com/mbovel/formal-verification-project/tree/main/paper-test-implementation): a toy optimizing DPLL solver in Scala, following Robert Nieuwenhuis and Albert Oliveras' [*On SAT modulo theories and optimization problems*](https://dl.acm.org/doi/10.1007/11814948_18) (SAT 2006). Part of *Minimizing Stainless counter-examples*, CS550 Formal Verification course project, EPFL, Winter 2021–2022.
