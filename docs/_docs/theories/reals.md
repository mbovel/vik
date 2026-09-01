---
title: Real arithmetic
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# Real arithmetic

Linear arithmetic over the reals: the same sums and comparisons as
[integer arithmetic](integers.md), with strict bounds meaning what they say.

**Decision procedure: yes.** The simplex decides linear real arithmetic directly — no integrality, so none
of the integer page's caveat — and a disequality splits into two strict bounds, finitely.

## What you can write

```smt2
(set-logic QF_LRA)
(declare-const x Real)
(declare-const y Real)
(assert (< 0.1 x))
(assert (< x (/ 1 3)))
(assert (= y (* 2.5 x)))
(check-sat)
(get-value (x y))
```

A strict bound is exact, not approximated: the solver works with δ-rationals — `x < c` is `x ≤ c − δ` for an
infinitesimal δ — and resolves a concrete δ for the model, so `0 < x < 1/3` answers a rational strictly
inside the window.

Rational literals — decimals and quotients like `(/ 1 3)` — become fresh constants defined by an integer
equation (`3 k = 1`), and a rational coefficient (`(* 2.5 x)`, `(/ x 2)`) is defined the same way, so the
solver still computes over integers throughout. A division or multiplication that is not linear is refused.

Reals and integers do not mix in a term: a comparison is integer or real by the sorts of its sides, and
`to_real` is not supported. Disequalities over reals split strictly: `x ≠ c` branches into `x < c` or
`x > c`.
