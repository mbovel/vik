---
title: The solver
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# The solver

`net.bovel.vik` plugs the parts together: the polarity-aware
[simplifier](../theories/first-order-logic.md) that runs before encoding, the encoder (Tseitin, atoms, the
theory combination, the lemmas of datatypes and lambdas), the theories over [the e-graph](egraph.md) and
[the LIA solver](lia.md) driven by [the CDCL(T) loop](cdclt.md), models, and the player that runs a parsed
script on a solver.

```scala
import net.bovel.smt2.Parser
import net.bovel.vik.Player

val script = Parser.parse("(declare-const x Int) (assert (> x 2)) (check-sat)")
Player.run(script)          // List("sat"): the responses, in order
```

`Player.answers(script, fuel = ...)` gives `Some(true)`, `Some(false)` or `None` per `check-sat`;
`Player.play` is the short form when unknown cannot happen.

A script can also ask why and what: after an `unsat`, `(get-unsat-core)` names the guilty
`(assert (! ... :named n))` assertions — re-asserting just those stays unsat — and after a `sat`,
`(get-model)` prints a `define-fun` per declared constant and function, and `(get-value (t ...))` evaluates
terms under that model. In Scala the same is `solver.unsatCore`, `solver.model()`, `model.value(term)` and
`model.holds(formula)` on a [`net.bovel.vik.Solver`](https://matt.bovel.net/vik/api/net/bovel/vik.html),
which keeps one engine across the `check-sat`s of a script: what one check learns, the next one keeps.

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
[smtlib/](https://github.com/mbovel/vik/tree/main/smtlib), with one test per file: it holds the instances of
the benchmarks, the three smallest files of every family of the release in our fragment, and the regression
suites of cvc5, z3 and princess, each file keeping its origin and its licence.
