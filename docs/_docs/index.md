---
title: VIK
extraJS:
  - js/snippets.js
extraCSS:
  - css/snippets.css
---

# VIK

A small SMT solver in Scala 3: equality with uninterpreted functions, linear integer arithmetic, algebraic
datatypes, lambdas, and the arithmetic fragment of bit-vectors — under 5000 lines, meant to be read.

The snippets of this site run here, in your browser: VIK is linked to WebAssembly (800 kB) and answers them.
Press **Run**; the scripts are editable, so change them and run them again.

```smt2
(declare-fun f (Int) Int)
(declare-const x Int)
(assert (= (f x) 1))
(assert (not (= (f (+ x 0)) 1)))
(check-sat)
```

- [A tour of the solver](guide.md), in SMT-LIB scripts.
- The [API documentation](../api/index.html) of the six modules.
- The [repository](https://github.com/mbovel/vik), with the design notes and the benchmark results.
