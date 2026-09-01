---
title: The S-expression reader
extraCSS:
  - css/site.css
---

# The S-expression reader

`net.bovel.sexpr` reads S-expressions from a string: atoms, lists, `|quoted symbols|`, `;` comments and
string literals, in one pass, with no token list materialised. It is the only thing under the SMT-LIB
parser, and it is usable alone:

```scala
import net.bovel.sexpr.SExprParser

SExprParser.parse("(a (b c)) (d)") // List(SList(...), SList(...))
```
