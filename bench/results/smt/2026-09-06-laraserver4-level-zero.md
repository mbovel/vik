# A level-0 unsat kept, the budget honoured, arrays paired under any function, 2026-09-06, laraserver4

Three wrong answers of the second audit ([issue 37](https://github.com/mbovel/vik/issues/37),
[issue 38](https://github.com/mbovel/vik/issues/38), [issue 39](https://github.com/mbovel/vik/issues/39)):
a conflict found at decision level 0 was not remembered, so the check-sat after an unsat answered sat; a
budget spent before the lemma loop let the search run without the array lemmas; two arrays met only as
the arguments of a function into a non-arithmetic sort were never paired, so extensionality could not
equate them. The fixes are a flag in the CDCL solver, an early `Unknown`, and a wider filter in the
sharing pass — the last the only one with a cost to look for, a pair of atoms per two applications of a
function with array arguments.

Cold, VIK on the JVM, the 44 evaluation instances, the median of three runs, one process per run:

| Instance | Before (09d9215) | After |
|---|---:|---:|
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | 0.26 s | 0.25 s |
| `20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd1570193` | 4.57 s | 4.65 s |
| `swap__swap_invalid_t3_np_nf_ai_00002_009.cvc` | 0.32 s | 0.32 s |
| `check__array_incompleteness1` | 0.37 s | 0.36 s |
| `2018-Goel-hwbench__QF_BV_cav14_example_cc_ref_max` | 0.34 s | 0.34 s |

Over the whole list 29.03 s before, 28.99 s after, no instance moving more than the noise (15 %).

**Kept.**
