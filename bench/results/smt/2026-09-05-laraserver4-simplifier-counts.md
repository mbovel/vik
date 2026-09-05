# The simplifier's counts follow a substitution, 2026-09-05, laraserver4

The simplifier eliminates a constant defined by an equality when every occurrence of it over the whole set
of assertions lies in the conjunction at hand. The occurrences were counted once, before any substitution,
and a substitution moves occurrences into the conjunction: after `c := d`, the count of `d` there matched
its stale total, `d` passed as local, and a conjunction of equalities was simplified to true while another
assertion still mentioned `d`. A contradiction answered sat ([issue 12](https://github.com/mbovel/vik/issues/12)).
The counts now move with every substitution, by the difference of the conjunction's counts before and after.

More constants are eliminated than before, and one of them showed a gap in the model: a definition whose
right-hand side is a constructor application was valued through the constructor's graph, whose default is
an abstract element, so `get-value` printed `@D!0` for `(data 0)`. A constructor application is now a value
once its arguments are.

The incremental instances, where the simplifier runs at every check, cold, the median of three, against
the committed run of 2026-09-04: `safari__mux` 0.67 → 0.68 s, `safari__illinois` 0.91 → 0.91,
`kind__MESI_i4` 1.61 → 1.62, `kind__car_6_e2_893` 2.05 → 2.09, `bmc__stack_k20` 0.77 → 0.77. Nothing moves.

**Kept.**
