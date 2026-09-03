---
title: The solver
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
---

# The solver

`net.bovel.vik` plugs the parts together, starting with its own trees: the solver's term language —
integers in linear normal form — that the surface language of
[`net.bovel.smtlib`](smtlib.md) cannot reach except through `Lowering`, one function from script to script.
The lowering is the compiler: names become indices and substitutions, chained atoms become pairwise
bounds, a term-level `ite` a defined constant, rational literals defined `Real` constants,
[bit-vectors](../theories/bit-vectors.md) linear integer arithmetic — and what the fragment excludes is
rejected here, with a message. Then the polarity-aware
[simplifier](../theories/first-order-logic.md) that runs before encoding, the encoder (Tseitin, atoms, the
theory combination, the lemmas of datatypes and arrays), the theories over
[the e-graph](egraph.md) and [the LIA solver](lia.md) driven by [the CDCL(T) loop](cdclt.md), models, and
the session that runs a script — either language, in pieces — on a solver.

```scala
import net.bovel.vik.Session

val s = Session()
s.run("(declare-const x Int) (declare-const y Int)") // any commands, as text; the responses come back
s.assert("(< x y)")                                  // in the scope the earlier pieces declared
s.checkSat()                                         // Some(true); None for unknown
s.assert(formula)                                    // a `net.bovel.smtlib.Formula`, lowered the same way
```

A `Session` keeps one parser, one lowering and one `Solver` alive together, so the declarations,
macros and defined constants of one piece serve the next; `push`, `pop`, `model()` and `unsatCore` are the
same commands as calls, and an `Smt2Error` names a symbol the text has not declared. `Session.run(script)`
runs a whole parsed script in a session of its own and returns the responses, and `Session.answers(script,
fuel = ...)` gives `Some(true)`, `Some(false)` or `None` per `check-sat`.

A script can also ask why and what. After an `unsat`, `(get-unsat-core)` names the guilty
`(assert (! ... :named n))` assertions: re-asserting just those, with the unnamed assertions, stays unsat.
The core is not minimal — it is the set of named assertions the search actually used — only named assertions
appear in it, it is empty when the unnamed assertions alone are unsat, and a named assertion lives and dies
with the `push` level that asserted it. After a `sat`, `(get-model)` prints a `define-fun` per declared
constant and function, and `(get-value (t ...))` evaluates terms under that model (a script that never asks
pays nothing for models; in Scala, `solver.produceModels` says whether a `Sat` takes one). In Scala the same
is `solver.unsatCore`, `solver.model()`, `model.value(term)` and `model.holds(formula)` on a
[`net.bovel.vik.Solver`](https://matt.bovel.net/vik/api/net/bovel/vik.html), which keeps one engine across
the `check-sat`s of a script: what one check learns, the next one keeps.

What a model holds: every declared constant, of every sort — an integer, a rational `(/ 1 3)`, a Boolean, an
element `@U!0` of an uninterpreted sort, a constructor term for a datatype; and a finite graph per declared
function or array, as an `ite` chain over the applications the assertions mention,
with a default for the rest. Its edges: a bit-vector constant is printed as its unsigned integer, with sort
`Int`, since the solver lowered it; a datatype component the assertions never touched is an element
`@List!0` rather than a constructor term; the constants the lowering introduced (`ite!1`, `floor!2`) are
printed with the rest; and `get-value` evaluates constants, literals and applications of
declared symbols, but an operation the lowering defines through an auxiliary constant — `ite`, `div`, `mod`,
the bit-vector operations — comes back as that constant's name (`ite!3`) rather than a value. After an
`unsat` or `unknown`, the answer is `(error "no model")`.

```smt2
(declare-const x Int)
(assert (! (<= x 2) :named low))
(assert (! (<= 4 x) :named high))
(check-sat)
(get-unsat-core)
```

```smt2
(declare-sort U 0)
(declare-fun f (U) U)
(declare-const a U)
(assert (= (f a) a))
(check-sat)
(get-model)
(get-value ((f (f a))))
```

The corpus of SMT-LIB files that the tests play is in
[corpus/](https://github.com/mbovel/vik/tree/main/corpus), with one test per file: it holds the instances of
the benchmarks, the three smallest files of every family of the release in our fragment, and the regression
suites of cvc5, z3 and princess, each file keeping its origin and its licence.
