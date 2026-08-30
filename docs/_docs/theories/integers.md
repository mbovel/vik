---
title: Integer arithmetic
extraJS:
  - js/snippets.js
extraCSS:
  - css/snippets.css
---

# Integer arithmetic

Linear arithmetic over the integers: sums with integer coefficients, compared with `<=`, `<`, `>=`, `>`, `=`
and `distinct`.

## What you can write

```smt2
(declare-const x Int)
(declare-const y Int)
(assert (<= (+ (* 2 x) (* 3 y)) 10))
(assert (>= (+ x y) 4))
(assert (>= x 0))
(assert (>= y 0))
(check-sat)
```

Integrality is decided, not approximated — this system has rational solutions and no integer one:

```smt2
(declare-const x Int)
(assert (< (* 2 x) 3))
(assert (> (* 2 x) 1))
(check-sat)
```

Multiplication of two unknowns is refused (this is linear arithmetic), as are reals; `div` and `mod` are not
supported yet.

Functions over integers mix the two theories, and the equalities travel both ways: arithmetic tells the
e-graph that two terms are equal, congruence tells arithmetic that two applications are:

```smt2
(declare-fun f (Int) Int)
(declare-const x Int)
(declare-const y Int)
(assert (= (+ x 1) y))
(assert (not (= (f (+ x 1)) (f y))))
(check-sat)
```

## Internals

Atoms are bounds. A formula `Σ aᵢ·xᵢ ⋈ c` becomes a bound on a slack variable of the
[simplex](../components/lia.md), which is Dutertre and de Moura's: the tableau is built once and never
changes, the bounds are the only state that a decision level pushes and pops, and a conflict is the row whose
bounds cannot be satisfied — a small, natural explanation to hand back to the SAT solver.

Integrality is branch and bound, iterative, over an explicit stack of branches, with the explanations of two
failed sides combined into one that does not mention the branch. Bounds are tightened by the gcd of their
row, and propagated between rows: from `x = Σ aᵢ·yᵢ`, the bounds of the `yᵢ` bound `x`, and the bound of `x`
with the others bounds each `yᵢ`. That propagation costs a factor of two to four on dense random systems and
is decisive on the instances of the library — `fft` goes from seventeen seconds to under one, `nec-smt` from
a timeout to under two.

The [combination with uninterpreted functions](uninterpreted-functions.md) is an arrangement decided by the
SAT solver: for every pair of shared integer terms that congruence could relate, their equality atom is an
atom of both theories — a merge or a disequality for the e-graph, two bounds or a disequality for the
simplex. Both theories are stably infinite, so a model of both, on the same arrangement, is a model of the
combination (Nelson and Oppen).
