---
title: The LIA solver
extraJS:
  - js/snippets.js
extraCSS:
  - css/snippets.css
---

# The LIA solver

A simplex over the integers: `net.bovel.lia`. It decides systems of bounds on linear combinations, and is
what answers the [arithmetic](../theories/integers.md) atoms.

## Using it

```scala
import net.bovel.lia.Solver

val solver = Solver()
val x = solver.newVar()
val y = solver.newVar()
val sum = solver.newSlack(List((BigInt(2), x), (BigInt(3), y))) // 2x + 3y

solver.assertUpper(sum, 10, reason = 1) // 2x + 3y <= 10
solver.assertLower(x, 1, reason = 2)    // x >= 1
solver.check()                          // Sat, Unsat(reasons) or Unknown
solver.value(x)                         // a solution, after Sat
```

`assertDistinct(x, c, reason)` says `x ≠ c`, decided by branching; `push()` and `pop()` frame the bounds of a
decision level (the tableau is built once and never undone); `keepGoing` bounds the work. Every `Unsat`
carries the reasons of the bounds that are inconsistent, which is the explanation the theory hands back.

## What is in it

| Feature | | Notes |
|---|---|---|
| Dutertre–de Moura simplex: tableau built once, bounds as the only backtracked state, Bland's rule | ✓ | |
| Integers only: bounds tightened by the gcd of an expression, no δ-rationals | ✓ | |
| Branch and bound, iterative, explanations combined across branches | ✓ | |
| Disequalities `x ≠ c`, decided at the final check by branching `x ≤ c − 1 ∨ x ≥ c + 1` | ✓ | for the equality atoms of shared terms |
| Bound propagation, incremental over the tightened variables | ✓ | [2–4× slower on random systems, decisive on SMT-LIB instances](https://github.com/mbovel/vik/blob/main/bench/results/lia/2026-08-29-laraserver4-propagation.md) |
| `Long` fast path for rationals | ✓ | [1.4–1.6×](https://github.com/mbovel/vik/blob/main/bench/results/lia/2026-08-29-laraserver4-experiments.md) |
| Branching on the most fractional unknown | ✗ | [mixed](https://github.com/mbovel/vik/blob/main/bench/results/lia/2026-08-29-laraserver4-experiments.md) |
| Cuts | ✗ | not yet (`cut_lemmas`, `wisa`) |
| Theory propagation of bounds as literals | ✗ | not yet |
| Combination with `egraph` | ✓ | arrangement by SAT: an equality atom per pair of shared integer terms, watched by both theories (a merge or a disequality for UF, two bounds or a disequality for LIA), in `smt`'s encoder; the solvers stay independent |

Results: [lia benchmarks](https://github.com/mbovel/vik/tree/main/bench/results/lia).
