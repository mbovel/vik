# Reals separated by a strict bound, 2026-09-05, laraserver4

Two soundness bugs read the reals as integers ([issue 7](https://github.com/mbovel/vik/issues/7),
[issue 8](https://github.com/mbovel/vik/issues/8)): `distinct` was lowered as the integers' apartness, and
the clause that decides a shared pair split at `c − 1`. Both forbid an open band of width one that the
reals allow, and both answered `unsat` on satisfiable scripts. The fix gives each the strict bound the
simplex already carries as a delta rational.

The clause is where it costs. `e ∨ (d < c) ∨ ¬(d ≤ c)` puts two bounds on the same constant where the
integer clause put them on `c − 1` and `c`, and the search pays for it, so the implication between them
is stated as a clause of its own: `d < c` implies `d ≤ c`. Cold, the median of three, laraserver4:

| Instance | Before (unsound) | Strict split | With the implication |
|---|---:|---:|---:|
| cpachecker__list_false-unreach-call | 0.69 s | 1.97 s | 1.05 s |

Adding the other two implications of the equality (`e ⇒ d ≤ c`, `e ⇒ ¬(d < c)`, which the theory asserts
anyway when the atom is true) made it worse again, 1.32 s, and they are not kept.

The rest of the evaluation's real instances do not move: `RandomDecoupled__pb_real` 0.88 → 0.83 s,
`Chua-1-VC2-L-chunk-0005` 0.28 → 0.28, `keymaera__division_dijkstra-node701` 0.24 → 0.25,
`cpachecker__diskperf_simpl1` 0.36 → 0.35. Over the whole cold list the JVM total moves 28.25 → 29.66 s,
which is that one instance plus noise.

**Kept**, at 1.5× on one instance: the answers were wrong before.
