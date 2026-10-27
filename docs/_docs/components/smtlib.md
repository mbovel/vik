---
title: The language
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
---

# The language

`io.bovel.smtlib` is the language: SMT-LIB 2 as written, with precise types — a `Sort` enum, `Term` and
`Formula` trees whose operations are enums per theory (`ArithOp`, `BvOp`, the comparisons), n-ary where the
standard's `:left-assoc` and `:chainable` make applications n-ary, `let` with its names, `match` read as testers and selectors. The
parser reads and sorts; it compiles away sort parameters and `match`, and rewrites nothing else — the solver's
[lowering](vik.md) compiles the trees. It depends on nothing; the solver depends on it, not the other way
around.

Underneath is the reader, `sexpr.SExprParser.parse`, which turns text into `SExpr`s in one pass with no token
list materialised. That is the standard's own category (SMT-LIB 2.6, 3.2: every syntactic category is a
specialization of `⟨s_expr⟩`), and the tree holds its lexical classes — a symbol, a keyword, a numeral, a
decimal, a hexadecimal or binary literal, a string, a list — each carrying what it means rather than how it
was written, and writing itself back the way SMT-LIB reads it. It is usable alone:

```scala
import io.bovel.smtlib.sexpr.SExprParser

SExprParser.parse("(a (b c)) (d)") // List(SList(...), SList(...))
```

```scala
import io.bovel.smtlib.{Parser, Printer}

val script = Parser.parse("(declare-const x Int) (assert (> x 2)) (check-sat)")
Printer.print(script)       // the script as written: nothing is rewritten at parse time
```

`Printer.print` writes the tree back as SMT-LIB, faithfully. The compilation to the solver's trees
belongs to [the solver](vik.md) — `io.bovel.vik.lower file.smt2` prints a script that way, and
`(get-assertions)` in a running script answers with the lowered stack.
