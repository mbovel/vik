---
title: The front end
extraJS:
  - js/snippets.js
extraCSS:
  - css/snippets.css
---

# The front end

Two modules read and write SMT-LIB: `net.bovel.sexpr`, a reader for S-expressions, and `net.bovel.smt2`, the
parser, printer and player of scripts. Between them and the theories sits `net.bovel.smt`, which holds terms,
formulas, the simplifier and the solver that plugs the parts together.

## Using them

```scala
import net.bovel.smt2.{Parser, Player, Printer}

val script = Parser.parse("(declare-const x Int) (assert (> x 2)) (check-sat)")
Player.run(script)          // List("sat"): the responses, in order
Printer.print(script)       // the script as VIK plays it
```

`Player.answers(script, fuel = …)` gives `Some(true)`, `Some(false)` or `None` per `check-sat`;
`Player.play` is the short form when unknown cannot happen. `Printer.print` shows the script after the front
end's rewriting, which is where you see the [bit-vectors lowered](../theories/bit-vectors.md) to integers.

The S-expression reader is a module of its own, used by nothing else in the parser's hot path:

```scala
import net.bovel.sexpr.SExprParser

SExprParser.parseAll("(a (b c)) (d)") // List(SList(...), SList(...))
```

## What is in them

| Module | What it does |
|---|---|
| `sexpr` | Reads S-expressions from a string: atoms, lists, `\|quoted symbols\|`, `;` comments, string literals. One pass, no tokens materialised. |
| `smt` | `Term` and `Formula`, the normal form of linear combinations, the lowering of bit-vectors to integers, the simplifier (local constant elimination, polarity-aware), the encoder (Tseitin, atoms, the theory combination, the lemmas of datatypes and lambdas) and `Solver`. |
| `smt2` | The commands, the parser (sorts, datatypes, `let`, macros through `define-fun`, `ite` naming, and the bit-vector syntax, which it hands to the solver's lowering), the printer, and the player that runs a script on a solver. |

The corpus of SMT-LIB files that the tests play is in
[smtlib/](https://github.com/mbovel/vik/tree/main/smtlib), with one test per file: it holds the instances of
the benchmarks, the three smallest files of every family of the release in our fragment, and the regression
suites of cvc5, z3 and princess, each file keeping its origin and its licence.
