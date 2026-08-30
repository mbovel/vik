# The corpus of SMT-LIB files

Test inputs and benchmark instances, one directory per logic (`QF_UF`, `QF_LIA`, `QF_UFLIA`, `QF_BV`, `QF_DT`,
`HO`), then per origin. Every file starts with a comment naming its origin, its original path and its license;
`(set-info :status ...)` before each `check-sat` gives the expected answer (the incremental convention). The JVM
test `SmtLibTest` (`smt2/src/test/scalajvm`) registers one test per file it finds here: the answers must be the
statuses, within a fuel per `check-sat` (a number of solver steps — conflicts, pivots, propagations, branches —
so the outcome is the same on every machine). `ignore.txt` lists the files the solver is known not to answer
within that fuel, or to answer wrongly, with the reason: the test expects those to fail, and when one passes it
asks for its line to be removed, so a fixed file surfaces. The benchmarks (`bench/`) use the subset listed in
their `@Param`s.

## Origins and licenses

| Origin | Directory | What | License |
|---|---|---|---|
| SMT-LIB release 2025, non-incremental ([Zenodo 16740866](https://zenodo.org/records/16740866)) | `<logic>/smt-lib/` | the instances the benchmarks use, plus the three smallest files of every family in our fragment | CC BY 4.0 for the release; a family may state its own license in its header (Rodin and CLEARSY: CC BY-NC 4.0 — kept out of new imports) |
| SMT-LIB release 2025, incremental ([Zenodo 15493096](https://zenodo.org/records/15493096)) | `<logic>/smt-lib-incremental/` | the three smallest QF_UF and QF_UFLIA files of every family (several `check-sat` per file, `push`/`pop`) | CC BY 4.0 |
| [SMT-LIB benchmark submission](https://github.com/SMT-LIB/benchmark-submission), the staging repository for the 2026 release | `<logic>/submission/` | the files in our fragment (a snapshot: not the final 2026 release) | as stated in each file's `:license` header |
| [cvc5](https://github.com/cvc5/cvc5) regression tests, `test/regress/cli` | `<logic>/cvc5/` | every regression file in our fragment with `; EXPECT:` answers (options in `; COMMAND-LINE:` are ignored) | modified BSD, [LICENSES/cvc5.txt](LICENSES/cvc5.txt) |
| [z3test](https://github.com/Z3Prover/z3test), `regressions/smt2` | `<logic>/z3test/` | every file in our fragment whose `.expected.out` is made of answers | MIT, [LICENSES/z3test.txt](LICENSES/z3test.txt) |
| [princess](https://github.com/uuverifiers/princess), `testcases` | `<logic>/princess/` | every file in our fragment with an answer in its directory's `Answers` log | BSD, [LICENSES/princess.txt](LICENSES/princess.txt) |

Files of logics we do not support (arrays, strings, reals, quantifiers, ...) and files outside the fragment
`smt2` parses are not imported; the importer reports what it rejected, by error.

## Importing

`sbt "smt2/runMain net.bovel.smt2.smt2import <origin> <source root> [files per family]"` copies an origin's
files in (the origins' layouts and answer formats are in `Import.scala`): every candidate is parsed, only the
files in the supported fragment are copied, with the header and the statuses, and the rejected ones are
reported by error. After a change that affects the answers, run the corpus test and edit `ignore.txt` as it asks.

## The SMT-LIB instances of the benchmarks

| File | Family | Status | Workload |
|---|---|---|---|
| `eq_diamond__eq_diamond20.smt2`, `eq_diamond__eq_diamond50.smt2` | eq_diamond | unsat | diamonds of equalities: exponential without learning |
| `NEQ__NEQ004_size4.smt2` | NEQ | unsat | pigeonhole-like disequalities over functions |
| `PEQ__PEQ018_size4.smt2` | PEQ | unsat | permutation equalities |
| `SEQ__SEQ032_size3.smt2`, `SEQ__SEQ009_size10.smt2` | SEQ | unsat | sequence equalities |
| `QG-classification__qg5__iso_brn001.smt2`, `QG-classification__qg5__iso_icl662.smt2` | QG-classification | sat, unsat | quasigroup classification (`let`-heavy, large) |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2`, `2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2` | 2018-Goel-hwbench | sat | hardware verification (abstraction with `ite`) |
| `20170829-Rodin__smt3534838651727683253.smt2` | 20170829-Rodin | unsat | Rodin proof obligations |
| `20190906-CLEARSY__0016__00003.smt2` | 20190906-CLEARSY | sat | Atelier B proof obligations |
| `TypeSafe__z3.1184131.smt2` | TypeSafe | unsat | Sledgehammer / Isabelle |

QF_LIA, from `QF_LIA.tar.zst` of the same release, the same way:

| File | Family | Status | Workload |
|---|---|---|---|
| `CAV_2009_benchmarks__20-vars__problem__001.smt2` | CAV_2009_benchmarks | sat | random linear systems (Dillig, Dillig, Aiken 2009) |
| `cut_lemmas__10-vars__cut_lemma_01_001.smt2`, `cut_lemmas__20-vars__cut_lemma_01_001.smt2` | cut_lemmas | unsat | integer infeasibility needing cuts or search (the 20-variable one takes VIK 18 s: comparison only) |
| `pidgeons__pigeon-hole-6.smt2` | pidgeons | unsat | pigeonhole over integers |
| `calypto__problem-005675.cvc.2.smt2` | calypto | sat | hardware verification (Calypto) |
| `dillig__20-4.smt2` | dillig | sat | random systems |
| `mathsat__FISCHER1-1-fair.smt2` | mathsat | sat | bounded model checking of Fischer's protocol |
| `rings__ring_2exp4_3vars_2ite_unsat.smt2` | rings | unsat | modular arithmetic encoded with `ite` |
| `prime-cone__prime_cone_unsat_3.smt2` | prime-cone | unsat | small crafted cones |
| `CIRC__multiplier_prime__MULTIPLIER_PRIME_2.msat.smt2` | CIRC | sat | arithmetic circuits |
| `convert__convert-tiff2jpg-query-1831.smt2` | convert | unsat | program verification queries |
| `fft__Sz64_1160.smt2`, `nec-smt__small__int_from_list__prp-2-17.smt2`, `wisa__wisa1.smt2` | fft, nec-smt, wisa | sat, unsat, sat | larger structured instances (VIK 17 s, timeout, timeout: comparison only) |

QF_BV, from `QF_BV.tar.zst` of the same release: instances in the arithmetic fragment that `smt2` lowers to integers
(no bit-blasting; of 188 small files across 43 families, 53 were in the fragment: the others use `bvmul` or `bvand` of
two variables, `bvor`, `bvxor`, or division and shifts by a variable):

| File | Family | Status | Workload |
|---|---|---|---|
| `sage__app7__bench_8505.smt2` | sage | sat | symbolic execution queries (SAGE) |
| `bench_ab__a97test0001.smt2` | bench_ab | sat | crafted arithmetic |
| `bmc-bv__adpcm.smt2`, `bmc-bv__Prim_4.smt2` | bmc-bv | sat, unsat | bounded model checking |
| `crafted__bitops7.smt2`, `crafted__bv8.smt2` | crafted | sat, unsat | small crafted instances |
| `2018-Goel-hwbench__QF_BV_bv_bv_swap_two_cc_ref_max.smt2`, `2018-Goel-hwbench__QF_BV_cav14_example_cc_reg_max.smt2` | 2018-Goel-hwbench | sat, unsat | hardware verification |
| `20190311-bv-term-small-rw-Noetzli__bv-term-small-rw_17.smt2` | 20190311-bv-term-small-rw-Noetzli | unsat | rewriting lemmas |
| `pspace__ndist.a.20000.smt2` | pspace | unsat | wide bit-vectors: 0.3 ms in process for VIK, a 20 s timeout for z3, which bit-blasts |
| `bruttomesso__core__ext_con_004_001_0016.smt2`, `fft__Sz128_2823.smt2` | bruttomesso, fft | unsat, unsat | `extract`/`concat` chains and FFT: VIK exceeds 20 s (comparison only) |

QF_DT, from `QF_DT.tar.zst` of the same release (8,700 files in three families; 34 of the 35 small files tried are in
the fragment: monomorphic datatypes without `Int` or `Bool` fields):

| File | Family | Status | Workload |
|---|---|---|---|
| `20172804-Barrett__tests__v1__v1l20086.cvc.smt2`, `20172804-Barrett__tests__v10__v10l20080.cvc.smt2` | 20172804-Barrett | sat, sat | random constraints over lists and trees (jsat) |
| `20172804-Barrett__tests__v10__v10l60049.cvc.smt2`, `20172804-Barrett__typed__v3__typed_v3l30025.cvc.smt2` | 20172804-Barrett | unsat, unsat | idem |
| `20230720-blocksworld__from_0_1_3_to_1_1_2_bmc_0.smt2`, `..._bmc_1.smt2`, `..._bmc_2.smt2` | 20230720-blocksworld | unsat | bounded model checking of blocks-world plans (`ite` chains over datatypes) |
| `20230720-blocksworld__from_0_9_4_to_3_3_7_bmc_5.smt2`, `20230720-blocksworld__from_14_0_3_to_12_4_1_bmc_7.smt2` | 20230720-blocksworld | unsat | deeper bounds: VIK 2 s and 26 s (comparison only) |
| `20210312-Bouvier__vlsat3_b07.smt2` | 20210312-Bouvier | unsat | VIK and z3 both exceed 20 s (comparison only) |

QF_UFLIA, from `QF_UFLIA.tar.zst` of the same release (659 files: mathsat 454, wisas 108, Certora 76, TwoSquares 21),
uninterpreted functions over integers, the theory combination:

| File | Family | Status | Workload |
|---|---|---|---|
| `TwoSquares__smtlib.598294.smt2`, `TwoSquares__smtlib.602033.smt2` | TwoSquares | sat, unsat | Sledgehammer proof obligations (Isabelle) |
| `wisas__xs_5_5.smt2`, `wisas__xs_8_8.smt2`, `wisas__xs_20_20.smt2` | wisas | unsat, sat, sat | Wisconsin Safety Analyzer: format-string safety, counters as functions |
| `wisas__xs_38_38.smt2` | wisas | sat | idem, larger: VIK 50 s (comparison only) |
| `mathsat__Wisa__xs-09-05-2-4-4-5.smt2`, `mathsat__Wisa__xs-07-11-1-1-1-1.smt2` | mathsat/Wisa | sat, unsat | the same family, MathSAT's translation |
| `mathsat__Hash__hash_uns_04_18.smt2` | mathsat/Hash | unsat | hash-table invariants |
| `mathsat__Hash__hash_sat_09_08.smt2`, `mathsat__EufLaArithmetic__hard__hard13.smt2` | mathsat | sat, unsat | VIK exceeds 60 s (comparison only) |
| `20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFLIA.smt2` | 20230314-Jaroslav-Bendik-Certora | unknown (z3: sat) | smart-contract verification conditions (comparison only) |

Licenses, as stated in the files: 2018-Goel-hwbench [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/);
20170829-Rodin and 20190906-CLEARSY [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/); the other
families, including all the QF_LIA, QF_BV and QF_UFLIA ones, state a `:source` only; the QF_DT files and the Certora one state [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). See each file's `set-info` header for the authors.

## Datatype workloads (`AdtBench`)

Random instances built directly with the `smt` API (no SMT-LIB file), sizes `n` = 10 and 30:

| Benchmark | Workload | Exercises |
|---|---|---|
| `finite` | ten sets of `n` random formulas over `Color` (three constants) and `Pair` (two colors) with `n` pair constants, a mix of satisfiable and unsatisfiable | testers, selectors, injectivity and distinctness on finite sorts, the eager expansion of finite sorts |
| `lists` | ten chains of `n` lists `l_i = cons(a_i, l_{i+1})`, closed by `l_n = nil` or, for half of them, by `l_n = l_0` (unsat by acyclicity), with disjunctions on their heads | injectivity along a chain, the acyclicity check at the final check |

