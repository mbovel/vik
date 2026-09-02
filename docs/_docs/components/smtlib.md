---
title: The language
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# The language

`net.bovel.smtlib` is the language: SMT-LIB 2 as written, with precise types — a `Sort` enum, `Term` and
`Formula` trees whose operations are enums per theory (`ArithOp`, `BvOp`, the comparisons), n-ary where the
standard's `:left-assoc` and `:chainable` make applications n-ary, `let` with its names. The
parser reads and sorts; it rewrites nothing — the solver's
[lowering](vik.md) compiles the trees. It depends only on
[the S-expression reader](sexpr.md); the solver depends on it, not the other way around.

```scala
import net.bovel.smtlib.{Parser, Printer}

val script = Parser.parse("(declare-const x Int) (assert (> x 2)) (check-sat)")
Printer.print(script)       // the script as written: nothing is rewritten at parse time
```

`Printer.print` writes the tree back as SMT-LIB, faithfully. The compilation to the solver's trees
belongs to [the solver](vik.md) — `smt2lowered file.smt2` prints a script that way, and
`(get-assertions)` in a running script answers with the lowered stack.
