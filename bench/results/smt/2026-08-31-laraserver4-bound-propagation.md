# Theory propagation of bounds, 2026-08-31, laraserver4

The arithmetic theory implies the bound atoms its bounds already decide, instead of leaving them to the
search: after every relaxation check, an unassigned `x ≤ c` whose bounds force it (or forbid it) is
propagated with those bounds as its explanation.

- Host: laraserver4, AMD EPYC 9254, shared with other users.
- OpenJDK 17.0.20, Scala 3.10.0-RC1, sbt 2.0.8, JMH via sbt-jmh 0.4.8.
- JMH: 2 forks, 3 warmup + 3 measurement iterations of 1 s, average time per operation, one thread; 95%
  confidence interval over the six iterations. `OtherSolversBench.vik` (parsing excluded).
- Raw data: [before](2026-08-31-laraserver4-bound-propagation-before.json),
  [after](2026-08-31-laraserver4-bound-propagation.json).

| Instance | Before (ms/op) | After (ms/op) | Before / after |
|---|---:|---:|---:|
| `wisas__xs_8_8` | 62.744 ± 14.000 | 43.588 ± 12.900 | 1.44× |
| `wisas__xs_11_11` | 209.255 ± 63.333 | 122.379 ± 41.895 | 1.71× |
| `wisas__xs_5_5` | 13.553 ± 4.113 | 13.875 ± 5.080 | 0.98× |
| `mathsat__Wisa__xs-09-05-2-4-4-5` | 58.006 ± 1.996 | 51.308 ± 13.566 | 1.13× |
| `dillig__20-4` | 9.509 ± 0.919 | 9.892 ± 1.992 | 0.96× |
| `NEQ__NEQ004_size4` | 92.839 ± 13.197 | 96.059 ± 6.039 | 0.97× |

Kept: the two heaviest instances of the combination gain 1.4× and 1.7×, and the others do not move. The
instances that gain are those with many bound atoms over shared terms, where the search was deciding what the
bounds already knew: `wisas/xs_8_8` went from 6151 decisions for 199 conflicts to a search that mostly
propagates.

## What was measured and rejected

- **Implying the equalities of the arrangement too** (an unassigned `x = c` whose bounds pin it, or exclude
  it): 9.8× on `wisas/xs_8_8` (64.9 ms to 6.6 ms) and 1.9× on `mathsat/Wisa`, but `wisas/xs_5_5`,
  `wisas/xs_11_11` and `Wisa/xs-05-13` went from under a second to more than two minutes — an implied
  equality merges classes that the search then has to take apart again. Limiting the implications to small
  explanations (4, 8 or 16 literals) did not save those instances, so the cost is the merges, not the size of
  the clauses.
- **Sharing only the terms arithmetic constrains** (the interface of Nelson and Oppen, rather than every
  integer term that congruence could relate): all four `wisas` instances stopped finishing within a minute.
