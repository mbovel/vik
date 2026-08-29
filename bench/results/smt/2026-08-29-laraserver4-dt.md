# Datatypes, 2026-08-29, laraserver4

First records of the datatype theory (`AdtRules`, commit `6782b29`), 3 forks, 5 warmup and 5 measurement iterations of
1 s, ms/op with the 95% confidence interval. Raw JMH JSON: [2026-08-29-laraserver4-dt-adtbench.json](2026-08-29-laraserver4-dt-adtbench.json),
[2026-08-29-laraserver4-dt-instances.json](2026-08-29-laraserver4-dt-instances.json).

## `AdtBench`: random formulas over finite datatypes, list chains

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| finite | n=10 | 3.016 | ± 0.202 | 6.7% | 2.688 | 3.813 | 3 × 5 |
| finite | n=30 | 8.724 | ± 0.382 | 4.4% | 8.289 | 11.035 | 3 × 5 |
| lists | n=10 | 0.626 | ± 0.041 | 6.6% | 0.571 | 0.778 | 3 × 5 |
| lists | n=30 | 2.050 | ± 0.224 | 10.9% | 1.733 | 2.955 | 3 × 5 |

## SMT-LIB QF_DT instances, in process

VIK on the tree (`vik`), z3 4.14.0 through its Java bindings on the tree (`z3Direct`, datatypes declared through
`mkDatatypeSorts`) and on the printed script (`z3`), cvc5 1.2.1 on the printed script; the ratio is VIK's time over the
solver's (above 1: faster than VIK).

| Instance | VIK | z3Direct | z3 | cvc5 |
|---|---:|---:|---:|---:|
| `20172804-Barrett__tests__v10__v10l20080.cvc` | 0.011 ± 0.001 | 0.995 ± 0.002 (0.01×) | 1.118 ± 0.006 (0.01×) | 2.869 ± 0.096 (0.00×) |
| `20172804-Barrett__tests__v10__v10l60049.cvc` | 0.022 ± 0.001 | 0.691 ± 0.004 (0.03×) | 0.811 ± 0.002 (0.03×) | 2.790 ± 0.202 (0.01×) |
| `20172804-Barrett__tests__v1__v1l20086.cvc` | 0.008 ± 0.000 | 0.739 ± 0.002 (0.01×) | 0.853 ± 0.004 (0.01×) | 1.624 ± 0.091 (0.00×) |
| `20172804-Barrett__typed__v3__typed_v3l30025.cvc` | 0.029 ± 0.002 | 0.736 ± 0.003 (0.04×) | 0.855 ± 0.003 (0.03×) | 2.047 ± 0.098 (0.01×) |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0` | 0.128 ± 0.010 | 1.052 ± 0.005 (0.12×) | 1.161 ± 0.006 (0.11×) | 2.828 ± 0.150 (0.05×) |
| `20230720-blocksworld__from_11_0_0_to_6_4_1_bmc_1` | 0.469 ± 0.034 | 1.442 ± 0.011 (0.33×) | 1.543 ± 0.010 (0.30×) | 3.580 ± 0.176 (0.13×) |
| `20230720-blocksworld__from_5_4_17_to_12_6_8_bmc_2` | 80.479 ± 1.669 | 2.637 ± 0.016 (30.52×) | 2.712 ± 0.023 (29.68×) | 7.046 ± 0.182 (11.42×) |

## One process per solver and instance, 60 s limit

`bench/compare.sh 60 'Barrett|blocksworld|Bouvier'`, JVM start-up included for VIK, princess and SMTInterpol; raw data in
[compare-raw-dt.tsv](compare-raw-dt.tsv).

| Instance | Expected | VIK | z3 | cvc5 | princess | SMTInterpol |
|---|---|---:|---:|---:|---:|---:|
| `20172804-Barrett__tests__v10__v10l20080.cvc.smt2` | sat | 0.24 s | 0.02 s | 0.01 s | 0.80 s | 0.11 s |
| `20172804-Barrett__tests__v10__v10l60049.cvc.smt2` | unsat | 0.23 s | 0.02 s | 0.01 s | 0.55 s | 0.11 s |
| `20172804-Barrett__tests__v1__v1l20086.cvc.smt2` | sat | 0.22 s | 0.02 s | 0.01 s | 0.64 s | 0.11 s |
| `20172804-Barrett__typed__v3__typed_v3l30025.cvc.smt2` | unsat | 0.24 s | 0.02 s | 0.01 s | 0.73 s | 0.10 s |
| `20210312-Bouvier__vlsat3_b07.smt2` | unsat | timeout | timeout | 51.90 s | timeout | timeout |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2` | unsat | 0.27 s | 0.02 s | 0.01 s | 0.67 s | 0.11 s |
| `20230720-blocksworld__from_0_9_4_to_3_3_7_bmc_5.smt2` | unsat | 1.89 s | 0.03 s | 0.06 s | timeout | 0.72 s |
| `20230720-blocksworld__from_11_0_0_to_6_4_1_bmc_1.smt2` | unsat | 0.31 s | 0.02 s | 0.01 s | 0.94 s | 0.14 s |
| `20230720-blocksworld__from_14_0_3_to_12_4_1_bmc_7.smt2` | unsat | 26.67 s | 0.14 s | 0.93 s | timeout | 2.40 s |
| `20230720-blocksworld__from_5_4_17_to_12_6_8_bmc_2.smt2` | unsat | 0.55 s | 0.03 s | 0.01 s | 1.29 s | 0.31 s |

- The Barrett/jsat instances (random constraints over lists and trees) are trivial for every solver; VIK in process is
  under a millisecond there, which the fixed cost of a z3 or cvc5 context (0.6–2 ms) exceeds.
- The blocksworld instances are bounded model checking over datatypes: chains of `ite`s and testers, so the SAT search
  dominates; VIK stays within a small factor of z3 on the shallow bounds, and falls behind on the deeper ones
  (`bmc_5` 2 s, `bmc_7` 26 s per process, where z3 needs a fraction of a second).
- `vlsat3_b07` (Bouvier) defeats VIK and z3 alike within 60 s.
