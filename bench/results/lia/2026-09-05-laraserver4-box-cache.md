# The small-model box keyed on what it reads, 2026-09-05, laraserver4

The box that bounds branch and bound (Papadimitriou's small-model bound, as a bit length) was cached
against the tableau version alone, while it is computed from the asserted bounds as well: a bound with a
large constant asserted after a check that pivoted nothing left the cached box too small, and a solution
past it was pruned as none. Through the `lia` API, `3x − 2y = 1` with `x ≥ 10^40` after such a check
answered `Unsat`; the audit of 2026-09-04 had found it by reading (seven agents), and no script-level
witness triggered it, since the encoder's own pivots refresh the cache more often than not. It is
`lia/src/test/scala/io/bovel/lia/SmallModelBoxTest.scala` now.

The fix keeps the cache and keys it on everything the box reads: the tableau version, the number of
variables, and the largest bound constant ever asserted, which the box uses in place of the bounds in
force, with two bounds counted per variable. A larger box is always sound, since the bound is monotone
in its entries, and those keys move rarely once a problem's constants have been seen.

Cold, the median of three, against the committed run of 2026-09-04, the instances where branch and
bound and the cache matter:

| Instance | Committed | Fixed |
|---|---:|---:|
| wisas__xs_8_8 | 0.44 s | 0.44 s |
| mathsat__Wisa__xs-09-05-2-4-4-5 | 0.50 s | 0.50 s |
| kind__MESI_i4 | 1.61 s | 1.60 s |
| kind__car_6_e2_893 | 2.05 s | 2.12 s |
| safari__mux | 0.67 s | 0.65 s |
| safari__illinois | 0.91 s | 0.91 s |
| bmc__stack_k20 | 0.77 s | 0.77 s |
| cut_lemmas__10-vars__cut_lemma_01_001 | 0.28 s | 0.29 s |

Nothing moves. **Kept.**
