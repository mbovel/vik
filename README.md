# The Verification Intermediate Kit (VIK)

VIK is a minimal but reasonably fast SMT solver for the theories of uninterpreted functions (UF), algebraic data
types (ADTs), linear integer arithmetic (LIA) and the arithmetic fragment of bit-vectors (BV, lowered to integers),
and their combinations. It is designed to be used as a backend for verification tools, and to be easy to understand and to modify.

## Design principle

VIK is meant to be minimal: its codebase is small, modular and easy to understand, with clarity preferred over
performance. It should also be verification-friendly; verifying parts of VIK is future work.

Performance still matters: the target is a factor of one to five of state-of-the-art solvers such as Z3 or cvc5 on
typical instances, and ten or more on pathological ones is acceptable. Every optimization is measured before it is
kept (see the [benchmarks](#benchmarks)), and several measured wins were dropped for readability (see
[Features](#features)).

## Components

| Package | Description |
|---|---|
| `egraph` | A standalone congruence closure over dense integer ids, with disequalities, backtracking (`push`/`pop`) and explanations. |
| `cdclt` | A CDCL(T) SAT solver abstracting over theory solvers: two watched literals, first-UIP learning, VMTF, phase saving, restarts. |
| `smt` | Terms and formulas, a simplifier, and the solver that plugs the e-graph (equality with uninterpreted functions, and algebraic datatypes as rules of the e-graph) and the LIA solver into `cdclt` as theories: QF_UF, QF_DT, QF_LIA and their combination (QF_UFLIA, datatypes with `Int` fields): integer leaves are LIA variables, applications are e-graph nodes, and an equality atom per pair of shared terms, tied to the bounds on their difference, lets the SAT solver choose an arrangement both theories accept. Higher-order core (SMT-LIB 2.7): function sorts `(-> S T)`, lambdas as terms with de Bruijn indices, beta-reduction at construction and by lemmas, partial applications, extensionality by fresh witnesses, and `Bool` as a sort (formulas as arguments and lambda bodies). |
| `smt2` | The SMT-LIB 2 front end: parses scripts into commands and plays them on the solver (`sbt "smt2/run file.smt2"` prints the answers of the `check-sat` commands). Bit-vectors are not bit-blasted: the arithmetic fragment of QF_BV (`bvadd`, `bvsub`, `bvneg`, `bvnot`, `bvmul`, shifts, `bvudiv` and `bvurem` by constants, masks, `extract`, `concat`, extensions, unsigned and signed comparisons) is lowered to linear integer arithmetic with bounded overflow counts; the rest is rejected. Datatypes (`declare-datatype(s)`, constructors, selectors, testers) are decided by the solver. `check-sat-assuming`, `reset-assertions`, `reset`; `(set-option :rlimit n)` sets the fuel of each `check-sat` and `:timeout` a deadline in milliseconds, `unknown` then, with `(get-info :reason-unknown)` answering `resourceout` or `timeout`. |
| `sexpr` | The S-expression lexer and parser used by `smt2`. |
| `lia` | Bounds on linear integer expressions, decided by Dutertre and de Moura's simplex with bound propagation and branch and bound. |
| `playground` | Two browser pages (Scala.js): an e-graph playground and an SMT-LIB playground. |
| `bench` | The JMH benchmarks of every module and the comparison with other solvers, with results per module. |

## Usage

The examples below are checked by [`smt2/src/test/scala/net/bovel/smt2/ReadmeExamplesTest.scala`](smt2/src/test/scala/net/bovel/smt2/ReadmeExamplesTest.scala).

### The `smt` solver

Formulas are built from terms with the operators of `net.bovel.smt` (`===`, `!==`, `<=`, `<`, `+`, `-`, `*`, `&&`,
`||`, `!`, `==>`), and asserted on a `Solver`, which answers `Sat` or `Unsat`. Uninterpreted functions:

```scala
import net.bovel.smt.*
import net.bovel.smt.Term.{App, Const, Num}

val solver = Solver()
val (a, b, f) = (Const("a"), Const("b"), Const("f"))
solver.assert(a === b)
solver.assert(App(f, List(a)) !== App(f, List(b)))
solver.solve() // solver.Result.Unsat: f(a) = f(b) by congruence
```

Linear integer arithmetic, on the same solver, with `Int`-valued constants:

```scala
val (x, y) = (Const("x"), Const("y"))
val lia = Solver()
lia.assert(x + y <= Num(1))
lia.assert(x >= Num(1))
lia.assert(y >= Num(1))
lia.solve() // lia.Result.Unsat
```

Levels of assertions with `push` and `pop`:

```scala
val s = Solver()
s.assert(x + y <= Num(3))
s.assert(x >= Num(1))
s.solve() // Sat
s.push()
s.assert(y >= Num(3))
s.solve() // Unsat
s.pop()
s.solve() // Sat again
```

Bit-vectors go through the SMT-LIB front end, which lowers them to integers (there is no bit-vector term in `smt`):

```scala
import net.bovel.smt2.{Parser, Player}

val script = Parser.parse("""
  (declare-const x (_ BitVec 8))
  (assert (= (bvadd x #xFF) #x00))
  (assert (bvult #x00 x))
  (check-sat)""")
Player.play(script) // List(true): x = #x01
```

### The `smt2` front end

`Parser.parse` turns an SMT-LIB script into a `Script` of commands (`declare-fun`, `assert`, `push`, `pop`,
`check-sat`, ...), `Player.play` runs it on a `Solver` and returns the answers of the `check-sat` commands, and
`Printer.print` prints a script back (after lowering: a QF_BV script comes out as QF_LIA):

```scala
import net.bovel.smt2.{Parser, Player, Printer}

val script = Parser.parse("""
  (set-logic QF_UF)
  (declare-sort U 0)
  (declare-fun f (U) U)
  (declare-const a U)
  (assert (= (f (f a)) a))
  (assert (not (= (f (f (f (f a)))) a)))
  (check-sat)""")
Player.play(script)   // List(false): unsat
Printer.print(script) // the script as text, one command per line
```

### The `cdclt` solver alone

A SAT solver over variables `0 until numVars`; `Lit(v)` is the positive literal, `Lit(v, false)` the negative one. A
theory is plugged in through the `Theory` trait (see `smt.UfTheory` and `smt.LiaTheory`).

```scala
import net.bovel.cdclt.{Lit, Solver as Sat}

val sat = Sat(numVars = 3)
sat.addClause(Lit(0), Lit(1))               // x0 ∨ x1
sat.addClause(Lit(0, false), Lit(2))        // ¬x0 ∨ x2
sat.addClause(Lit(1, false), Lit(2, false)) // ¬x1 ∨ ¬x2
sat.solve() match
  case Sat.Result.Sat(model) => model.toList // one satisfying assignment, e.g. List(true, false, true)
  case Sat.Result.Unsat => Nil
```

### The `egraph` alone

Classes are `Int` ids; an application is a node over the classes of a function symbol and its arguments. Merges
carry an `Int` reason (a literal, in the solver) and `explain` returns the reasons an equality follows from.

```scala
import scala.collection.immutable.ArraySeq
import net.bovel.egraph.{App, EGraph}

val eg = EGraph()
val (f, a, b) = (eg.newClass(), eg.newClass(), eg.newClass())
val (fa, fb) = (eg.add(App(f, ArraySeq(a))), eg.add(App(f, ArraySeq(b))))
eg.merge(a, b, reason = 1)
eg.rebuild()
eg.find(fa) == eg.find(fb) // true, by congruence
eg.explain(fa, fb)         // ArrayBuffer(1)
eg.push()
eg.disequate(fa, fb, reason = 2)
eg.conflict                // Some(ArrayBuffer(1, 2)): the reasons of the inconsistency
eg.pop()                   // the disequality is gone
```

`EGraphBuilder` does the same with named symbols (`node("f", sym("a"))`) and prints the e-graph in DOT.

### The `lia` solver alone

Integer unknowns, expressions over them as slack variables, and bounds asserted with an `Int` reason; `check` decides
and `value` reads a solution, an `Unsat` names the reasons of an inconsistent subset of bounds.

```scala
import net.bovel.lia.Solver as Lia

val lia = Lia()
val (x, y) = (lia.newVar(), lia.newVar())
val s = lia.newSlack(List((BigInt(2), x), (BigInt(2), y))) // s stands for 2x + 2y
lia.assertLower(s, 3, reason = 0)                            // 2x + 2y ≥ 3, tightened to x + y ≥ 2
lia.assertUpper(s, 3, reason = 1)                            // 2x + 2y ≤ 3, tightened to x + y ≤ 1: Unsat(Set(0, 1))
```

## Features

Ticks are what is implemented; the rest says why not, with the commit or the measurement.

### `egraph`

| Feature | | Notes |
|---|---|---|
| Dense `Int` ids, eager union-find (union by size, circular member lists) | ✓ | as Z3 and cvc5 |
| Deferred congruence rebuild with a pending list (egg) | ✓ | |
| `java.util.HashMap` as the congruence table | ✓ | 5% faster than Scala's, [measured](bench/results/egraph/2026-08-28-laraserver4-scala-hashmap.md) |
| Backtracking: `push`/`pop` over an undo trail | ✓ | |
| Disequalities (forbid lists) and conflicts | ✓ | |
| Explanations: union tree, most recent edge below the LCA (LISA's scheme), linear LCA | ✓ | [`f3d89a4`](https://github.com/mbovel/vik/commit/f3d89a4) |
| Event log and per-class atom lists for theory propagation | ✗ | 1.8× on one instance, nothing elsewhere, 85 subtle lines: removed for readability ([`6badd0f`](https://github.com/mbovel/vik/commit/6badd0f) → [`bf015d6`](https://github.com/mbovel/vik/commit/bf015d6), [measurements](bench/results/smt/2026-08-29-laraserver4-scan.md)) |
| Cached node hashes | ✗ | [no gain](bench/results/egraph/2026-08-28-laraserver4-cached-hash.md) |
| Union-tree edges as flat arrays | ✗ | [no gain](bench/results/egraph/2026-08-28-laraserver4-flat-edges.md) |
| Rules: callbacks on added nodes and merges, derived equalities with premises as justification, contradictions, a final check | ✓ | [docs/adt-plan.md](docs/adt-plan.md); the datatype theory is such a rule set |

### `smt`: datatypes (`AdtRules`)

| Feature | | Notes |
|---|---|---|
| Injectivity and distinctness of constructors, on merge | ✓ | premises: the two constructor nodes are equal |
| Selectors of the constructor node of their argument's class | ✓ | on the selector's addition or when the class gains the node |
| Testers: true on the right constructor, contradiction on another, expansion `t = C(s_1(t), ..., s_n(t))` when true | ✓ | |
| Exhaustiveness: some tester of every datatype term holds, as a clause; finite sorts expanded eagerly, so the search is complete on them | ✓ | `Unit`/`Option` cardinality examples in the tests |
| Acyclicity, at the final check | ✓ | depth-first search over constructor nodes |
| Differential test against brute force over finite datatypes | ✓ | 200 random formulas |
| Fields of sort `Int` (and bit-vectors, lowered to `Int`) | ✓ | through the UF–LIA combination |
| Fields of sort `Bool` | ✓ | Boolean terms |
| Parametric datatypes | ✗ | rejected by the parser |

### `smt`: lambdas

| Feature | | Notes |
|---|---|---|
| `(lambda ((x S) ...) body)` as terms, de Bruijn indices: alpha-equivalent lambdas are the same term, and equal lambdas are congruent | ✓ | `Term.Lambda`, `Term.Var`; function sorts `(-> S ... T)`, curried applications |
| Beta-reduction: syntactic redexes reduced at construction (parser, simplifier substitutions), those the search makes (`f = l` decided) as lemmas `f ≠ l ∨ f(args) = l(args)`, to a fixpoint | ✓ | `Term.app`, `Encoder.instantiateLambdas`; nothing in the e-graph |
| Soundness: no atom on a term with a bound variable, so bound variables are never equated with anything | ✓ | argument at `Term.instantiate`; tested with the finite-sort expansion |
| Extensionality: a fresh witness `c` per equality between functions in the assertions, `f = g ∨ f(c) ≠ g(c)` | ✓ | `Encoder.assertExtensionality`; `λx. f(x) = f` follows through beta |
| Curried and partial applications: `(f 1 2)` is `((f 1) 2)`, `(f 1)` a function; function sorts `(-> S T)` flattened | ✓ | `Term.app`, `Sorts`; partial applications get the same lemmas as lambdas |
| `Bool` as a sort: formulas as arguments, `ite` branches, datatype fields and lambda bodies (`λx. x > 0`) | ✓ | `Term.Bool`: a leaf class tied to its formula's literal; every Boolean term is decided and merged with `true` or `false` (two-valued, so congruence sees equal Booleans) |
| `ite` and bit-vector lowering under lambdas | ✗ | they introduce side assertions, which would mention bound variables |

### `cdclt`

| Feature | | Notes |
|---|---|---|
| Two watched literals | ✓ | [1.7–4.6×](bench/results/cdclt/2026-08-28-laraserver4-2wl.md) |
| First-UIP learning and backjumping | ✓ | trail invariants after CreuSAT |
| VMTF decisions, phase saving, Luby restarts | ✓ | [measured](bench/results/cdclt/2026-08-28-laraserver4-restarts.md) (pigeonhole gets slower) |
| Theory interface: assert, propagate, explain lazily, push/pop, final check; backjump to a conflict's level | ✓ | [`7b748a8`](https://github.com/mbovel/vik/commit/7b748a8) |
| Learned clause minimization | ✗ | [within noise, 0.75× on pigeonhole](bench/results/cdclt/2026-08-29-laraserver4-minimization.md) |
| Clause deletion | ✗ | would only help the hard tail (`SEQ009`); not worth the bookkeeping yet |
| VSIDS | ✗ | VMTF instead, as CaDiCaL and CreuSAT |
| Assumptions and unsat cores | ✗ | not yet |
| A budget: `solve(keepGoing)` asked at every decision, conflict, pivot, propagation and branch; `Unknown` when it says stop | ✓ | `smt.Solver.solve(fuel)`: deterministic steps, `Unknown(Fuel)`; a deadline is a `keepGoing` with a clock, `Unknown(Stopped)` |

### `lia`

| Feature | | Notes |
|---|---|---|
| Dutertre–de Moura simplex: tableau built once, bounds as the only backtracked state, Bland's rule | ✓ | |
| Integers only: bounds tightened by the gcd of an expression, no δ-rationals | ✓ | |
| Branch and bound, iterative, explanations combined across branches | ✓ | |
| Disequalities `x ≠ c`, decided at the final check by branching `x ≤ c − 1 ∨ x ≥ c + 1` | ✓ | for the equality atoms of shared terms |
| Bound propagation, incremental over the tightened variables | ✓ | [2–4× slower on random systems, decisive on SMT-LIB instances](bench/results/lia/2026-08-29-laraserver4-propagation.md) |
| `Long` fast path for rationals | ✓ | [1.4–1.6×](bench/results/lia/2026-08-29-laraserver4-experiments.md) |
| Branching on the most fractional unknown | ✗ | [mixed](bench/results/lia/2026-08-29-laraserver4-experiments.md) |
| Cuts | ✗ | not yet (`cut_lemmas`, `wisa`) |
| Theory propagation of bounds as literals | ✗ | not yet |
| Combination with `egraph` | ✓ | arrangement by SAT: an equality atom per pair of shared integer terms, watched by both theories (a merge or a disequality for UF, two bounds or a disequality for LIA), in `smt`'s encoder; the solvers stay independent |

## Requirements

- A JDK (17 or later)
- [sbt](https://www.scala-sbt.org/) 2
- [Node.js](https://nodejs.org/) 24 or later, for the Scala.js tests (WebAssembly) and the playground
- [LLVM](https://llvm.org/) with clang, for Scala Native: on the path, or `LLVM_BIN` set to its `bin` directory before starting sbt (an LLVM release unpacked in a user directory works; its clang may need `libtinfo5`, which the Ubuntu archive's package provides in user space with `LD_LIBRARY_PATH`)

## Development

Subprojects: `egraph`, `cdclt`, `lia`, `sexpr`, `smt` and `smt2` on the JVM, the same with a `JS` suffix on Scala.js (same sources, linked to WebAssembly: Scala.js's WebAssembly backend, run by Node.js 24 or later) and with a `Native` suffix on Scala Native (same sources, compiled with clang), `playground` (the browser playgrounds, on the JavaScript backend) and `bench` (the benchmarks, below).

```sh
sbt compile   # compiles all subprojects
sbt test      # runs the tests on the JVM, on Scala.js (WebAssembly) and on Scala Native
```

> [!NOTE]
> sbt 2 caches task results by content: when nothing changed since the last run, `sbt test` reports `Passed: Total 0` instead of re-running the tests, and `clean` does not reset this cache. To force a run, use `sbt "egraph/testOnly *"` or `sbt "egraphJS/testOnly *"`.

Other sbt 2 edges met here (thin client hangs, forked processes outliving a killed client, virtual classpath references) are collected in [sbt2-issues.md](sbt2-issues.md).

The SMT-LIB files used by the tests and the benchmarks are the corpus in [smtlib/](smtlib/README.md), by logic and origin (SMT-LIB releases, the 2026 submission repository, and the regression suites of cvc5, z3 and princess, with their licenses). The JVM test `SmtLibTest` registers one test per file: the answers must be the file's statuses within a fuel (solver steps, so the same on every machine); `smtlib/ignore.txt` names the files known to fail, with the reason, and the test asks for a line to be removed when its file passes.

### Benchmarks

[JMH](https://github.com/openjdk/jmh) benchmarks for every module live in the `bench` subproject, with the workloads and results kept per module:

```sh
sbt "bench/Jmh/run -l"                # lists the benchmarks
sbt "bench/Jmh/run .*EGraphBench.*"   # the benchmarks matching a regex
```

See [bench/README.md](bench/README.md) for running and result files, and [bench/results/egraph](bench/results/egraph/README.md),
[cdclt](bench/results/cdclt/README.md), [lia](bench/results/lia/README.md), [sexpr](bench/results/sexpr/README.md),
[smt2](bench/results/smt2/README.md) and [smt](bench/results/smt/README.md) (SMT-LIB instances and the comparison with
z3, cvc5, princess and SMTInterpol, in process through their Java bindings and as separate processes) for the workloads and current results.

### Playgrounds

Build them, serve the output directory, and open <http://localhost:8000/classes/index.html>: an e-graph playground
(equalities in, the e-graph drawn) and an SMT-LIB playground (a script in, the answers and the lowered script out).

```sh
sbt "playground/copyResources; playground/fastLinkJS"
python3 -m http.server --directory target/out/sjs1/scala-3.9.0/playground 8000
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
