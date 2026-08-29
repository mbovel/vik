# The Verification Intermediate Kit (VIK)

VIK is a minimal but reasonnable SMT solver for the theories of linear integer arithmetic (LIA), algebraic data types (ADTs) and uninterpreted functions (UFs). It is designed to be used as a backend for verification tools, and to be easily understandable and modifiable.

## Design Principle

VIK is meant to be a minimal: its codbase is small, modular, and easy to understand, with a focus on clarity over performance. It should also be verification-friendly; verifying parts of VIK is future work.

While not optimized for performance, its performance should be within an order of magnitude of state-of-the-art solvers such as Z3 or CVC5.

## Components

- E-graph (`egraph`): a standalone congruence closure with disequalities, backtracking and explanations.
- CDCL(T) solver (`cdclt`): a SAT modulo theory solver abstracting over theory solvers.
- SMT solver (`smt`): terms and formulas, a simplifier, and the solver plugging the e-graph into `cdclt` as the theory of equality with uninterpreted functions (QF_UF).
- SMT-LIB front end (`smt2`, on the S-expression parser `sexpr`): parses SMT-LIB 2 scripts into commands and plays them on the solver; `sbt "smt2/run file.smt2"` prints the answers of the `check-sat` commands.
- LIA solver (`lia`): bounds on linear integer expressions, decided by Dutertre and de Moura's simplex with branch and bound; standalone for now, to be plugged into `cdclt` by `smt`.

## Requirements

- A JDK (17 or later)
- [sbt](https://www.scala-sbt.org/) 2
- [Node.js](https://nodejs.org/), for the Scala.js tests and the playground

## Development

Subprojects: `egraph`, `cdclt`, `lia`, `sexpr`, `smt` and `smt2` on the JVM, the same with a `JS` suffix on Scala.js (same sources), `egraphPlayground` (a browser playground for the e-graph), and the benchmark projects below.

```sh
sbt compile   # compiles all subprojects
sbt test      # runs the tests on the JVM and on Scala.js
```

> [!NOTE]
> sbt 2 caches task results by content: when nothing changed since the last run, `sbt test` reports `Passed: Total 0` instead of re-running the tests, and `clean` does not reset this cache. To force a run, use `sbt "egraph/testOnly *"` or `sbt "egraphJS/testOnly *"`.

### Benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks live in one subproject per component, each with a README describing the workloads and holding the results:

```sh
sbt "egraphBench/Jmh/run"            # all benchmarks of the e-graph (about 15 minutes)
sbt "egraphBench/Jmh/run .*star.*"   # only the benchmarks matching a regex
```

- [egraph-bench](egraph-bench/README.md): the e-graph.
- [cdclt-bench](cdclt-bench/README.md): the CDCL(T) solver on random k-SAT and pigeonhole instances.
- [lia-bench](lia-bench/README.md): the LIA solver on random systems of bounds.
- [sexpr-bench](sexpr-bench/README.md) and [smt2-bench](smt2-bench/README.md): the parsers.
- [smt-bench](smt-bench/README.md): SMT-LIB QF_UF instances end to end, and a comparison with z3, cvc5, princess and SMTInterpol.

### Playground

Build it, serve its output directory, and open <http://localhost:8000/classes/index.html>:

```sh
sbt "egraphPlayground/copyResources; egraphPlayground/fastLinkJS"
python3 -m http.server --directory target/out/sjs1/scala-3.9.0/egraph-playground 8000
```

## References and Inspirations

- Sarek Høverstad Skotåm. [*CreuSAT: Using Rust and Creusot to create the world's fastest deductively verified SAT solver*](https://www.duo.uio.no/handle/10852/96757) (master's thesis, University of Oslo, 2022) and [CreuSAT](https://github.com/sarsko/CreuSAT) (with Robinson, a verified DPLL solver): the trail as a stack with invariants (reason clauses post-unit with the implied literal first, learned clauses as resolvents keeping the database equisatisfiable, SAT as a complete conflict-free assignment), which `cdclt` follows to stay verifiable.
- Bruno Dutertre and Leonardo de Moura. [*A Fast Linear-Arithmetic Solver for DPLL(T)*](https://doi.org/10.1007/11817963_11) (CAV 2006): the simplex of `lia`, with the tableau set up once and bounds as the only backtracked state, and its explanations from the bounds of a violated row.
- Robert Nieuwenhuis, Albert Oliveras and Cesare Tinelli. [*Solving SAT and SAT Modulo Theories: From an Abstract Davis–Putnam–Logemann–Loveland Procedure to DPLL(T)*](https://doi.org/10.1145/1217856.1217859) (JACM 2006): the CDCL(T) architecture of `cdclt`, whose theory interface follows Z3's `theory` and cvc5's `Theory` (assert literals in trail order, propagate with conflicts and implied literals, explain lazily, push and pop per decision level).
- Robert Nieuwenhuis and Albert Oliveras. [*Proof-Producing Congruence Closure*](https://doi.org/10.1007/978-3-540-32033-3_33) (RTA 2005): explanations of equalities from a proof forest whose edges carry the equated terms and a reason (an input equality or a congruence).
- Simon Guilloud, Sankalp Gambhir and Viktor Kunčak. [`Congruence.scala`](https://github.com/epfl-lara/lisa/blob/58cc866dfb23645e8d40108ed18a8225c8e6424c/lisa-sets/src/main/scala/lisa/automation/Congruence.scala) in [LISA](https://github.com/epfl-lara/lisa): a proof-producing congruence closure that reads explanations off the union tree, splitting at the most recent union on the path to the lowest common ancestor instead of re-rooting a proof forest. `EGraph.explain` does the same.
- George Zakhour, Pascal Weisenburger, Jahrim Gabriele Cesario and Guido Salvaneschi. [*Dis/Equality Graphs*](https://doi.org/10.1145/3704913) (POPL 2025): disequalities as per-class "forbid lists" (after Greg Nelson, *Techniques for Program Verification*, PhD thesis, Stanford, 1980), moved along on merge, a conflict being a class that forbids itself. Used by `EGraph.disequate`.
- Nikolaj Bjørner, Clemens Eisenhofer, Arie Gurfinkel, Nuno P. Lopes, Leonardo de Moura, Lev Nachmanson and Christoph Wintersteiger. [*Z3 Internals (Draft)*](https://z3prover.github.io/papers/z3internals.html).
- Zachary DeVito. [*Intro to EGraphs*](https://colab.research.google.com/drive/1tNOQijJqe5tw-Pk9iqd6HHb2abC5aRid) (`egraphs_example.ipynb`): a Google Colab notebook with an executable Python prototype of the e-graph described in the [egg paper](https://egraphs-good.github.io).
- Sankalp Gambhir. [*smt-lia-pl-seminar*](https://github.com/sankalpgambhir/smt-lia-pl-seminar): building a small SAT solver with theory support from scratch. Talk given at the EPFL graduate PL seminar, 2025-05-13 and 2025-05-20.
- Matthieu Bovel and Solal Pirelli. [*paper-test-implementation*](https://github.com/mbovel/formal-verification-project/tree/main/paper-test-implementation): a toy optimizing DPLL solver in Scala, following Robert Nieuwenhuis and Albert Oliveras' [*On SAT modulo theories and optimization problems*](https://dl.acm.org/doi/10.1007/11814948_18) (SAT 2006). Part of *Minimizing Stainless counter-examples*, CS550 Formal Verification course project, EPFL, Winter 2021–2022.
