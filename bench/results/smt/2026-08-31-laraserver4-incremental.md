# One solver across the script's check-sats, 2026-08-31, laraserver4

The solver now persists across the `check-sat`s of a script: terms stay encoded, the e-graph and the
simplex keep their state, the SAT search keeps every learned clause, and a script level is a selector
literal — `push` allocates it, an assertion of the level is guarded by it, `check-sat` assumes the open
ones, `pop` retires it as a false unit. Before, every `check-sat` built a fresh solver.

- Host: laraserver4, AMD EPYC 9254, shared with other users; OpenJDK 17.0.20, Scala 3.10.0-RC1.
- JMH `OtherSolversBench.vik`, 2 forks, 2 warmup + 3 measurement iterations of 1 s; means over the raw
  samples. Cold rows: the CLI on the JVM, wall clock. "Before" is the table of the evaluation page as
  committed at 1e4cbea (same machine, same day).
- Raw data: [incremental.json](2026-08-31-laraserver4-incremental.json),
  [incremental-2.json](2026-08-31-laraserver4-incremental-2.json).

| Instance | Before | After | Before / after |
|---|---:|---:|---:|
| `kind/car_6` (cold JVM, 21 checks) | 5.38 s | 3.71 s | 1.45× |
| `CLEARSY__0020` (warm, 181 checks) | 11.97 ms | 9.60 ms | 1.25× |
| `Certora` QF_UFDTLIA (cold JVM) | 14.3 s | 14.8 s | 0.97× |
| `kind/MESI_i4` (warm, 5 checks) | 455 ms | 666 ms | 0.68× |
| `NEQ__NEQ004_size4` (warm, one check) | 98.3 ms | 112.8 ms | 0.87× |
| `bug-1_ab_reg_max` (warm, one check) | 5 µs | 6 µs | 0.82× |
| `wisas__xs_8_8`, `dillig__20-4` (warm) | | | within noise |

Kept (the user's call): the wins are where learning transfers — the BMC scripts whose unsat steps
repeat — and the losses are documented and inside the project's 1–5×-of-z3 envelope. The `MESI_i4` cost
is structural: atoms of popped levels stay registered, and the arithmetic implies its bounds by a scan
of all of them per round, so a check pays for the history before it (per-check times inside that one
file: 116, 173, 88, 288, 595 ms).

## What was measured and rejected

- **An event-driven index for the UF atoms** (candidate lists per e-class, fed by the e-graph's merge
  hook, drained instead of the full scan): did not move `MESI_i4` — its accumulation is on the
  arithmetic side — and missed implications from congruent `add`s (the hook reports merges, not
  add-time congruences), which sent a higher-order test into a timeout. Reverted; the arithmetic twin
  (~60 lines into `lia.Solver`) was not attempted.
- **A fast path through the theory combination** (impliers remembered only when a shared atom exists,
  a lone theory's implications passed through unbuffered): about 4 ms of the 14 on `NEQ004`, kept for
  its simplicity, not decisive.
- **Early exits for untouched theories** (a decision that concerns no atom costs no scan): kept — it
  recovered the small single-check queries (`bug-1` from 11 µs back to 6 µs) — but it does not help a
  busy search.

The single-shot path also changed shape: the whole-set simplification now runs only when the first
`check-sat` sees one level (its constant elimination is only sound while nothing else mentions the
constant, so a later mention rebuilds the encoder), and both theories are always attached.
