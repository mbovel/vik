# State decided at the first solve, 2026-09-05, laraserver4

Two soundness bugs remembered a decision taken at the first `check-sat` ([issue 13](https://github.com/mbovel/vik/issues/13),
[issue 14](https://github.com/mbovel/vik/issues/14)): whether the problem has array stores was a memoized
value, and a Boolean term was known as two-valued only once it reached the e-graph as an argument or the
side of an equality. A store, or a Boolean argument, first seen at a later solve missed its axioms, and a
contradiction answered sat.

The fixes: the store test is a method over the declarations, and a Boolean term is registered as two-valued
where its predicate literal is created, so its first assertion already merges it with `true` or `false`.
The second turns a recorded disequality into a merge for every false predicate atom, which is the cost to
watch on the uninterpreted-function instances.

Cold, the median of three, laraserver4, against the committed run of 2026-09-04, every instance of the
evaluation whose logic has uninterpreted functions, arrays or datatypes (28 of the 44):

| Instance | Committed | With the fixes |
|---|---:|---:|
| QG-classification__qg5__iso_icl662 | 0.42 s | 0.43 s |
| QG-classification__qg5__iso_brn001 | 0.35 s | 0.35 s |
| 2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max | 0.25 s | 0.25 s |
| 20170829-Rodin__smt3534838651727683253 | 0.26 s | 0.27 s |
| NEQ__NEQ004_size4 | 0.65 s | 0.67 s |
| 20230314-Jaroslav-Bendik-Certora__65782 | 4.62 s | 4.59 s |
| storeinv__storeinv_t2_np_nf_ai_00001_001 | 0.28 s | 0.29 s |
| bmc__stack_k20 | 0.77 s | 0.77 s |
| kind__car_6_e2_893 | 2.05 s | 2.08 s |
| safari__illinois | 0.91 s | 0.91 s |

The other eighteen are within a hundredth of a second as well. Nothing moves: the predicate atoms that
now merge were already deciding the same classes through their applications.

**Kept.**
