# In-process comparison: VIK, z3 and cvc5 through their Java bindings, princess, SMTInterpol, 2026-08-29, laraserver4

`OtherSolversBench`: one parsed script per instance, solved in the same JVM and thread (3 forks, 5 warmup and
5 measurement iterations of 1 s), ms/op with the 95% confidence interval. The ratio is VIK's time over the solver's:
above 1 the solver is faster than VIK. What each column does with the parsed script:

- **VIK**: solves the tree (`vik`). Records: [2026-08-29-laraserver4-scan.json](2026-08-29-laraserver4-scan.json) for the
  QF_UF and QF_LIA instances, [2026-08-29-laraserver4-bv.json](2026-08-29-laraserver4-bv.json) for QF_BV.
- **z3Direct**: builds z3's AST from the tree through the Java API (`Z3Encoder`) and checks it; **z3**: prints the
  script and has z3 parse it. Both create and close a `Context` per operation. z3 4.14.0 through
  `org.sosy-lab:javasmt-solver-z3` (JNI). Raw data: [2026-08-29-laraserver4-inprocess.json](2026-08-29-laraserver4-inprocess.json).
- **cvc5**: prints the script and runs it through cvc5's parser API, a `Solver` per operation; cvc5 1.2.1 through
  `org.sosy-lab:javasmt-solver-cvc5` (JNI).
- **princess** and **smtinterpol**: the printed script parsed by the solver; measured on subsets only (princess needs
  minutes per operation on the crafted QF_UF instances). Records: [2026-08-29-laraserver4.md](2026-08-29-laraserver4.md).

| Instance | VIK | z3Direct | z3 | cvc5 | princess | smtinterpol |
|---|---:|---:|---:|---:|---:|---:|
| `20170829-Rodin__smt3534838651727683253` | 0.004 ± 0.000 | 0.722 ± 0.001 (0.01×) | 0.848 ± 0.002 (0.00×) | 1.901 ± 0.099 (0.00×) | — | — |
| `2018-Goel-hwbench__QF_BV_bv_bv_swap_two_cc_ref_max` | 0.282 ± 0.018 | 2.225 ± 0.007 (0.13×) | 2.363 ± 0.007 (0.12×) | 4.999 ± 0.117 (0.06×) | — | — |
| `2018-Goel-hwbench__QF_BV_cav14_example_cc_reg_max` | 0.491 ± 0.028 | 2.976 ± 0.013 (0.16×) | 3.094 ± 0.017 (0.16×) | 8.195 ± 0.607 (0.06×) | — | — |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max` | 0.004 ± 0.000 | 0.787 ± 0.002 (0.00×) | 0.930 ± 0.006 (0.00×) | 2.382 ± 0.085 (0.00×) | — | — |
| `2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max` | 2.718 ± 0.037 | 19.710 ± 0.109 (0.14×) | 20.261 ± 0.142 (0.13×) | 185.885 ± 10.375 (0.01×) | 26298.613 ± 1578.022 (0.00×) | 31.419 ± 0.969 (0.09×) |
| `20190311-bv-term-small-rw-Noetzli__bv-term-small-rw_17` | 0.004 ± 0.000 | 0.657 ± 0.001 (0.01×) | 0.786 ± 0.002 (0.01×) | 1.577 ± 0.098 (0.00×) | — | — |
| `20190906-CLEARSY__0016__00003` | 0.003 ± 0.000 | 0.936 ± 0.007 (0.00×) | 1.114 ± 0.006 (0.00×) | 9.200 ± 0.546 (0.00×) | 19.657 ± 0.711 (0.00×) | 1.154 ± 0.061 (0.00×) |
| `CAV_2009_benchmarks__20-vars__problem__001` | 2.460 ± 0.107 | 2.464 ± 0.010 (1.00×) | 2.564 ± 0.008 (0.96×) | 27.792 ± 0.138 (0.09×) | — | — |
| `CIRC__multiplier_prime__MULTIPLIER_PRIME_2.msat` | 0.159 ± 0.007 | 2.035 ± 0.005 (0.08×) | 2.175 ± 0.015 (0.07×) | 2.694 ± 0.059 (0.06×) | — | — |
| `NEQ__NEQ004_size4` | 94.021 ± 2.760 | 12.284 ± 0.037 (7.65×) | 12.094 ± 0.030 (7.77×) | 112.497 ± 0.635 (0.84×) | — | 117.696 ± 3.363 (0.80×) |
| `PEQ__PEQ018_size4` | 123.788 ± 2.387 | 37.668 ± 0.060 (3.29×) | 38.199 ± 0.052 (3.24×) | 188.952 ± 0.835 (0.66×) | — | — |
| `QG-classification__qg5__iso_brn001` | 0.677 ± 0.011 | 3.513 ± 0.013 (0.19×) | 3.199 ± 0.070 (0.21×) | 9.319 ± 0.385 (0.07×) | — | — |
| `QG-classification__qg5__iso_icl662` | 3.703 ± 0.061 | 9.200 ± 0.030 (0.40×) | 8.290 ± 0.207 (0.45×) | 27.400 ± 0.991 (0.14×) | 290.438 ± 7.822 (0.01×) | 17.999 ± 0.277 (0.21×) |
| `SEQ__SEQ032_size3` | 10.833 ± 0.367 | 7.882 ± 0.037 (1.37×) | 7.893 ± 0.014 (1.37×) | 36.732 ± 0.133 (0.29×) | — | 11.473 ± 0.405 (0.94×) |
| `TypeSafe__z3.1184131` | 0.003 ± 0.000 | 0.723 ± 0.003 (0.00×) | 0.851 ± 0.004 (0.00×) | 2.505 ± 0.146 (0.00×) | 1.933 ± 0.063 (0.00×) | — |
| `bench_ab__a97test0001` | 0.004 ± 0.000 | 1.161 ± 0.017 (0.00×) | 1.272 ± 0.004 (0.00×) | 1.465 ± 0.076 (0.00×) | — | — |
| `bmc-bv__Prim_4` | 0.001 ± 0.000 | 0.613 ± 0.001 (0.00×) | 0.745 ± 0.001 (0.00×) | 1.240 ± 0.075 (0.00×) | — | — |
| `bmc-bv__adpcm` | 0.001 ± 0.000 | 0.619 ± 0.001 (0.00×) | 0.751 ± 0.001 (0.00×) | 1.262 ± 0.078 (0.00×) | — | — |
| `calypto__problem-005675.cvc.2` | 2.120 ± 0.083 | 2.995 ± 0.006 (0.71×) | 3.096 ± 0.006 (0.68×) | 6.774 ± 0.117 (0.31×) | — | — |
| `convert__convert-tiff2jpg-query-1831` | 0.396 ± 0.022 | 2.032 ± 0.009 (0.20×) | 2.058 ± 0.030 (0.19×) | 5.326 ± 0.077 (0.07×) | — | — |
| `crafted__bitops7` | 0.126 ± 0.002 | 1.983 ± 0.010 (0.06×) | 2.105 ± 0.010 (0.06×) | 3.926 ± 0.135 (0.03×) | — | — |
| `crafted__bv8` | 0.016 ± 0.001 | 1.191 ± 0.003 (0.01×) | 1.321 ± 0.004 (0.01×) | 1.742 ± 0.091 (0.01×) | — | — |
| `cut_lemmas__10-vars__cut_lemma_01_001` | 0.009 ± 0.000 | 1.490 ± 0.004 (0.01×) | 1.623 ± 0.002 (0.01×) | 1.682 ± 0.059 (0.01×) | — | — |
| `dillig__20-4` | 9.925 ± 0.144 | 2.625 ± 0.008 (3.78×) | 2.730 ± 0.007 (3.64×) | 30.921 ± 0.134 (0.32×) | — | — |
| `mathsat__FISCHER1-1-fair` | 0.628 ± 0.011 | 2.226 ± 0.016 (0.28×) | 2.330 ± 0.026 (0.27×) | 3.763 ± 0.140 (0.17×) | — | — |
| `pidgeons__pigeon-hole-6` | 0.319 ± 0.013 | 14.258 ± 0.023 (0.02×) | 14.390 ± 0.020 (0.02×) | 9.425 ± 0.473 (0.03×) | — | — |
| `prime-cone__prime_cone_unsat_3` | 0.057 ± 0.001 | 2.027 ± 0.008 (0.03×) | 2.153 ± 0.004 (0.03×) | 3.345 ± 0.043 (0.02×) | — | — |
| `pspace__ndist.a.20000` | 0.317 ± 0.013 | 22.721 ± 0.032 (0.01×) | 30.532 ± 0.047 (0.01×) | 3.939 ± 0.407 (0.08×) | — | — |
| `rings__ring_2exp4_3vars_2ite_unsat` | 52.442 ± 1.894 | 8.128 ± 0.023 (6.45×) | 8.682 ± 0.022 (6.04×) | 25.646 ± 0.392 (2.04×) | — | — |
| `sage__app7__bench_8505` | 0.005 ± 0.000 | 1.131 ± 0.003 (0.00×) | 1.260 ± 0.002 (0.00×) | 1.451 ± 0.079 (0.00×) | — | — |

- Through JNI, a z3 operation costs at least 0.6–0.9 ms (context creation, declarations, the check) and a cvc5 one
  1.2–2 ms, whatever the instance; VIK's in-process cost on the small instances is 1–5 µs. For a verifier that asks
  many small questions in process, that fixed cost is the whole story: VIK is one to two orders of magnitude faster
  there, and stays ahead on most medium instances (the QG-classification and Goel ones, calypto, the bit-vector ones;
  `pspace/ndist` 70×, where z3 bit-blasts and VIK reasons over integers).
- z3 is faster where the search dominates: NEQ004 (7.7×), rings (6×), dillig (3.8×), PEQ018 (3.3×), SEQ032 (1.4×);
  the CAV_2009 instance is a tie. That is the SAT core and the LIA search, the known gap.
- Parsing SMT-LIB text instead of building the AST costs z3 about 0.1 ms per instance (`z3` vs `z3Direct`), and
  much more on `pspace/ndist` (30 vs 23 ms): wide bit-vector literals.
- cvc5 through its parser API is 2–10× slower than z3 in process on these instances; princess is far behind
  (26 s on `mcs`); SMTInterpol sits between z3 and cvc5.
