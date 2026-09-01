---
title: The language
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# The language

`net.bovel.smt2` is the language layer: `Term` and `Formula` with the normal form of linear combinations,
the [lowering of bit-vectors](../theories/bit-vectors.md) to integers, and SMT-LIB 2 in and out — the
parser (sorts, datatypes, `let`, macros through `define-fun`, `ite` naming), the printer, and the script
model. It depends only on [the S-expression reader](sexpr.md); the solver depends on it, not the other way
around.

```scala
import net.bovel.smt2.{Parser, Printer}

val script = Parser.parse("(declare-const x Int) (assert (> x 2)) (check-sat)")
Printer.print(script)       // the script as VIK will play it, after the lowerings
```

`Printer.print` shows a script after the front end's rewriting, which is where you see the bit-vectors
lowered to integers; `(get-assertions)` in a running script answers the same way.
