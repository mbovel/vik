# The zero quotient of a signed division, 2026-09-05, laraserver4

A signed division by an unknown divisor negated its quotient and remainder as `2^n − q`, which is `2^n`
for a zero value, outside the range of the case constant: the definition was unsatisfiable and
`(bvsdiv #b0001 #b1110)` answered unsat ([issue 9](https://github.com/mbovel/vik/issues/9)).

Two correct forms were measured on `QF_BV/smt-lib/2020-Weber__extensions` (three signed divisions,
unsat), cold, one run each:

| Form | Time | Answer |
|---|---:|---|
| the bug (`2^n − q` unreduced) | 7.48 s | unsat |
| `q ≠ 0` in the condition, two bounds | 9.39 s | unsat |
| `q ≥ 1` in the condition, one bound | 7.79 s | unsat |
| the case ranging to `2^n`, reduced by `modulo` | 9.23 s | unsat |

**Kept**: `q ≥ 1`. In the corpus harness, warm and under two million steps of fuel, the file answered
with the bug and runs out of fuel with either correct form: the unsatisfiable branch pruned the search of
an unsat file. It is on `corpus/ignore.txt` as out of reach, with that reason.

The bit-vector instances of the evaluation, cold, the median of three, against the committed run of
2026-09-04: `20210219-Sydr__…__cjpeg__predicate_150` 2.77 → 2.77 s, `spear__cvs_vc71402` 0.40 → 0.40,
`sage__app2__bench_512` 0.39 → 0.40, `bench_ab__a649test0047` 0.33 → 0.35, `pspace__ndist.a.20000`
0.28 → 0.30, `2018-Goel-hwbench__cav14_example_cc_ref_max` 0.33 → 0.33. Nothing moves.
