# What a check-sat pays for, 2026-09-04, laraserver4

The warm table splits by workload: on the one-shot instances VIK's median is 0.17× z3's time, on the
incremental ones 6.85×. A script of 500 rounds of `push`, one trivial assertion, `check-sat`, `pop` over a
level-0 problem of 200 integers and 199 constraints showed why:

| Solver | 500 checks | Per check |
|---|---:|---:|
| VIK | 3.18 s | 6.3 ms |
| cvc5 | 1.08 s | 2.2 ms |
| z3 | 0.02 s | 0.04 ms |

A check that asserts one bound cost six milliseconds because three things were redone over the whole
problem at every check, while the search itself was 3% of the profile:

- **Bound propagation allocated an array per variable at every call**, to count the rows it had visited.
  The counts are now marks in an array that outlives the call, one mark per call.
- **The scan for implied atoms walked every atom**, asking the solver whether the bounds decided it. The
  bound atoms are now indexed by their variable, the solver records which variables' bounds were written
  (asserted, derived or undone by a pop), and the scan looks at those and at the atoms that are new.
- **The small-model box was recomputed from the whole tableau at every `check`**, multiplying the
  coefficients of every row. It is cached against a version the tableau bumps whenever a row is written,
  so a check that pivots nothing pays for it once.

The microbenchmark goes from 3.18 s to 2.6 s, and the corpus suite from 140 s to 122 s. Warm,
`VikBench.api` (JMH `-f 2 -wi 3 -i 3`, ms/op,
[before](2026-09-04-laraserver4-per-check-before.json), [after](2026-09-04-laraserver4-per-check-after.json)):

| Instance | Before | After | Ratio |
|---|---:|---:|---:|
| kind__car_6_e2_893.ec (incremental) | 1985.4 ± 301.9 | 981.4 ± 78.8 | 2.02× |
| safari__mux (incremental) | 108.2 ± 17.5 | 64.7 ± 13.4 | 1.67× |
| wisas__xs_8_8 | 8.89 ± 1.60 | 6.07 ± 1.05 | 1.46× |
| kind__MESI_i4.ec (incremental) | 277.4 ± 10.4 | 210.1 ± 9.5 | 1.32× |
| 20190906-CLEARSY__0020__00009 (incremental) | 9.32 ± 0.33 | 9.20 ± 0.31 | 1.01× |
| dillig__20-4 | 8.39 ± 0.41 | 8.32 ± 0.39 | 1.01× |

**Kept.** The incremental instances gain a third to a half of their time and the one-shot ones do not move,
except `wisas`, which asks many small checks of a problem that stays and gains like an incremental one.

Two micro-optimizations the flat profile suggested and the measurement refused, so that nobody tries them
again: a single-method interface for the fuel check, in place of the `() => Boolean` whose `apply` boxes
(within the noise warm, 8% *worse* cold), and `Array[Boolean]` in place of `ArrayBuffer[Boolean]` for the
theories' per-atom flags (within the noise). The JIT inlines both, and a boxed `Boolean` is a cached object,
so the 13% of samples the profiler puts on `boxToBoolean` is cold-code accounting, not time.

**What is left.** The per-check cost is no longer proportional to the problem, but it is still fifty times
z3's on the microbenchmark. Next: the clauses of a popped level are never deleted, so they stay in the
watch lists and their atoms stay in the decision order (measured 30× on a bit-vector script that pushes and
pops, `bench/results/smt/2026-09-04-laraserver4-retired-atoms.md`), and there is no learned-clause deletion
at all.
