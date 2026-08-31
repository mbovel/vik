---
title: The CDCL(T) solver
extraJS:
  - js/snippets.js
extraCSS:
  - css/site.css
  - css/snippets.css
---

# The CDCL(T) solver

A CDCL SAT solver with a theory interface: `net.bovel.cdclt`. It decides the propositional structure of the
[formulas](../theories/first-order-logic.md), and asks a theory about the atoms.

## Using it

```scala
import net.bovel.cdclt.{Lit, Solver, Theory}

val solver = Solver(numVars = 3)
solver.addClause(Lit(0), Lit(1))
solver.addClause(Lit(0, false), Lit(2))
solver.solve() // Sat(model) or Unsat
```

A theory implements `assertLit`, `propagate`, `finalCheck`, `explain`, `push` and `pop`: the solver asserts
the literals in trail order, asks it to check after every round of boolean propagation, and expects either a
conflict — literals that cannot hold together, learned as a clause — or the literals it implies, whose
explanation is asked for lazily. `Theory.Result.Unknown` stops the search, which is how a budget is enforced.

`solve(keepGoing)` asks at every decision and conflict whether to go on, so a fuel or a deadline is a
function you pass; `Solver(numVars, theory, restartInterval)` takes the Luby unit, which the tests use to
restart at every conflict.

## What is in it

| Feature | | Notes |
|---|---|---|
| Two watched literals | ✓ | [1.7–4.6×](https://github.com/mbovel/vik/blob/main/bench/results/cdclt/2026-08-28-laraserver4-2wl.md) |
| First-UIP learning and backjumping | ✓ | trail invariants after CreuSAT |
| VMTF decisions, phase saving, Luby restarts | ✓ | [measured](https://github.com/mbovel/vik/blob/main/bench/results/cdclt/2026-08-28-laraserver4-restarts.md) (pigeonhole gets slower) |
| Theory interface: assert, propagate, explain lazily, push/pop, final check; backjump to a conflict's level | ✓ | [`7b748a8`](https://github.com/mbovel/vik/commit/7b748a8) |
| Assumptions, variables that grow, repeated solves | ✓ | decided first, in order, MiniSat's scheme; what the [persistent solver](https://github.com/mbovel/vik/blob/main/bench/results/smt/2026-08-31-laraserver4-incremental.md) and the unsat cores stand on |
| Learned clause minimization | ✗ | [within noise, 0.75× on pigeonhole](https://github.com/mbovel/vik/blob/main/bench/results/cdclt/2026-08-29-laraserver4-minimization.md) |
| Clause deletion | ✗ | would only help the hard tail (`SEQ009`); not worth the bookkeeping yet |
| VSIDS | ✗ | VMTF instead, as CaDiCaL and CreuSAT |
| A budget: `solve(keepGoing)` asked at every decision, conflict, pivot, propagation and branch; `Unknown` when it says stop | ✓ | `smt.Solver.solve(fuel)`: deterministic steps, `Unknown(Fuel)`; a deadline is a `keepGoing` with a clock, `Unknown(Stopped)` |

Results: [cdclt benchmarks](https://github.com/mbovel/vik/tree/main/bench/results/cdclt).
