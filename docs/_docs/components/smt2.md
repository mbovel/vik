---
title: The language
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# The language

`net.bovel.smt2` is the language layer: `Term` and `Formula` with the normal form of linear combinations
and the [bit-vector operations](../theories/bit-vectors.md) as written, and SMT-LIB 2 in and out — the
parser (sorts, datatypes, `let`, macros through `define-fun`, `ite` naming), the printer, and the script
model. It depends only on [the S-expression reader](sexpr.md); the solver depends on it, not the other way
around.

```scala
import net.bovel.smt2.{Parser, Printer}

val script = Parser.parse("(declare-const x Int) (assert (> x 2)) (check-sat)")
Printer.print(script)       // the script as parsed: normalized, bit-vectors as written
```

`Printer.print` writes the tree back as SMT-LIB, bit-vector operations included. The lowering to
integers belongs to [the solver](vik.md) — `smt2lowered file.smt2` prints a script that way, and
`(get-assertions)` in a running script answers with the lowered stack.
