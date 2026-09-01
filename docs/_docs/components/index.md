---
title: Components
extraCSS:
  - css/site.css
---

# Components

VIK is a stack of small modules, and each one is usable on its own: a tool that needs congruence closure, or
a SAT solver with a theory of its own, or a simplex over the integers, can depend on that module alone.

| Module | What it is | Page |
|---|---|---|
| `net.bovel.egraph` | Congruence closure with backtracking, explanations, disequalities and rules | [The e-graph](egraph.md) |
| `net.bovel.cdclt` | A CDCL SAT solver with a theory interface | [The CDCL(T) solver](cdclt.md) |
| `net.bovel.lia` | A simplex over the integers with branch and bound | [The LIA solver](lia.md) |
| `net.bovel.sexpr` | S-expressions from a string, one pass | [The S-expression reader](sexpr.md) |
| `net.bovel.smt2` | Terms and formulas, the lowerings, SMT-LIB in and out | [The language](smt2.md) |
| `net.bovel.vik` | The encoder, the simplifier, the theories, the solver and the player | [The solver](vik.md) |

The [API documentation](../api/index.html) covers all of them.
