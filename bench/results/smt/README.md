# SMT benchmarks

End-to-end benchmarks of the SMT solver (`smt2` parser, `smt` solver, `cdclt`, `egraph`, `lia`) on QF_UF, QF_LIA, QF_BV and QF_DT instances of the
[SMT-LIB](https://smt-lib.org) benchmark library, and a comparison with other solvers.

## Instances

From the [SMT-LIB release 2025 (non-incremental benchmarks)](https://zenodo.org/records/16740866), `QF_UF.tar.zst`,
copied to [bench/src/main/resources/smtlib](../../src/main/resources/smtlib) with the family as a prefix (`family__file.smt2`).
One or two per family, chosen small enough for a JMH run and, where possible, one satisfiable and one unsatisfiable:

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

Licenses, as stated in the files: 2018-Goel-hwbench [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/);
20170829-Rodin and 20190906-CLEARSY [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/); the other
families, including all the QF_LIA and QF_BV ones, state a `:source` only; the QF_DT files state [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/). See each file's `set-info` header for the authors.

## Datatype workloads (`AdtBench`)

Random instances built directly with the `smt` API (no SMT-LIB file), sizes `n` = 10 and 30:

| Benchmark | Workload | Exercises |
|---|---|---|
| `finite` | ten sets of `n` random formulas over `Color` (three constants) and `Pair` (two colors) with `n` pair constants, a mix of satisfiable and unsatisfiable | testers, selectors, injectivity and distinctness on finite sorts, the eager expansion of finite sorts |
| `lists` | ten chains of `n` lists `l_i = cons(a_i, l_{i+1})`, closed by `l_n = nil` or, for half of them, by `l_n = l_0` (unsat by acyclicity), with disjunctions on their heads | injectivity along a chain, the acyclicity check at the final check |

## Running

From the repository root (see [bench/README.md](../../README.md) for the setup of `jmh2md.py`):

```sh
sbt "bench/Jmh/run -rf json -rff target/jmh-result.json .*SmtBench.solve .*OtherSolversBench.vik"   # VIK on every instance
sbt "bench/Jmh/run -p file=eq_diamond__eq_diamond50.smt2"     # one instance
bench/.venv/bin/python bench/jmh2md.py target/jmh-result.json
bench/compare.sh 60 > bench/results/smt/compare.tsv   # wall clock vs z3, cvc5, princess, SMTInterpol
```

The other benchmarks are recorded on subsets (`-p file=...`): `parse` and `smtinterpol` on a few representative instances,
`princess` on those it solves in seconds (it needs minutes on NEQ004, PEQ018 and SEQ032).

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

In process, `OtherSolversBench` runs princess and SMTInterpol (JVM libraries) and z3 and cvc5 through their Java bindings
as published by SoSy-Lab (the JavaSMT project) on Maven Central: the Java parts are dependencies of `bench`, the native
libraries are downloaded by `bench/fetch-native.sh` into `bench/native/` (Linux x86-64) and found through
`java.library.path`. `z3` parses the printed script, `z3Direct` builds z3's AST from the tree through the API (the
counterpart of `vik`, which solves the tree). `sbt "bench/runMain net.bovel.bench.SolverCheck"` runs every in-process
solver on every instance and reports disagreements with the expected status.

`compare.sh` runs each solver as a separate process with a timeout (default 60 s) and records the answer and the wall
clock, JVM start-up included for VIK, princess and SMTInterpol. z3 and cvc5 are taken from the `PATH`; princess and
SMTInterpol are fetched with [coursier](https://get-coursier.io).

## Current results

laraserver4 (AMD EPYC 9254, shared machine), OpenJDK 17.0.20, 2026-08-29. Setup and noise: [results/2026-08-29-laraserver4.md](2026-08-29-laraserver4.md). In-process comparison with z3 and cvc5 through their Java bindings, princess and SMTInterpol: [2026-08-29-laraserver4-inprocess.md](2026-08-29-laraserver4-inprocess.md) (a z3 operation costs at least 0.6 ms through JNI, a cvc5 one 1.2 ms; VIK is 1–5 µs on the small instances and ahead on most medium ones, z3 up to 7.7× faster on the search-heavy crafted ones). Process-level comparison with z3, cvc5, princess and SMTInterpol: [results/2026-08-29-laraserver4-compare.md](2026-08-29-laraserver4-compare.md) (QF_UF) and [results/2026-08-29-laraserver4-compare-lia.md](2026-08-29-laraserver4-compare-lia.md) (QF_LIA).

| Benchmark | Params | Mean (ms/op) | 95% CI | CI / mean | Min | Max | Forks × iterations |
|---|---|---:|---:|---:|---:|---:|---:|
| vik | file=NEQ__NEQ004_size4.smt2 | 40.217 | ± 1.046 | 2.6% | 38.280 | 44.249 | 3 × 5 |
| vik | file=PEQ__PEQ018_size4.smt2 | 120.365 | ± 2.705 | 2.2% | 115.977 | 131.112 | 3 × 5 |
| vik | file=SEQ__SEQ032_size3.smt2 | 11.293 | ± 0.222 | 2.0% | 10.880 | 11.887 | 3 × 5 |
| vik | file=QG-classification__qg5__iso_brn001.smt2 | 0.716 | ± 0.062 | 8.6% | 0.643 | 1.048 | 3 × 5 |
| vik | file=QG-classification__qg5__iso_icl662.smt2 | 4.517 | ± 0.099 | 2.2% | 4.316 | 4.779 | 3 × 5 |
| vik | file=2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2 | 0.004 | ± 0.000 | 3.1% | 0.004 | 0.005 | 3 × 5 |
| vik | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 2.507 | ± 0.184 | 7.3% | 2.155 | 3.191 | 3 × 5 |
| vik | file=20170829-Rodin__smt3534838651727683253.smt2 | 0.004 | ± 0.000 | 7.1% | 0.004 | 0.005 | 3 × 5 |
| vik | file=20190906-CLEARSY__0016__00003.smt2 | 0.003 | ± 0.001 | 16.5% | 0.003 | 0.006 | 3 × 5 |
| vik | file=TypeSafe__z3.1184131.smt2 | 0.004 | ± 0.000 | 5.8% | 0.003 | 0.004 | 3 × 5 |
| solve | file=NEQ__NEQ004_size4.smt2 | 41.285 | ± 1.447 | 3.5% | 38.182 | 46.117 | 3 × 5 |
| solve | file=PEQ__PEQ018_size4.smt2 | 122.575 | ± 2.292 | 1.9% | 117.500 | 127.752 | 3 × 5 |
| solve | file=SEQ__SEQ032_size3.smt2 | 11.625 | ± 0.361 | 3.1% | 10.880 | 12.777 | 3 × 5 |
| solve | file=QG-classification__qg5__iso_brn001.smt2 | 0.694 | ± 0.015 | 2.2% | 0.661 | 0.760 | 3 × 5 |
| solve | file=QG-classification__qg5__iso_icl662.smt2 | 4.401 | ± 0.058 | 1.3% | 4.277 | 4.662 | 3 × 5 |
| solve | file=2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2 | 0.004 | ± 0.000 | 2.4% | 0.004 | 0.004 | 3 × 5 |
| solve | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 2.563 | ± 0.187 | 7.3% | 2.211 | 3.296 | 3 × 5 |
| solve | file=20170829-Rodin__smt3534838651727683253.smt2 | 0.004 | ± 0.000 | 6.4% | 0.004 | 0.005 | 3 × 5 |
| solve | file=20190906-CLEARSY__0016__00003.smt2 | 0.003 | ± 0.000 | 5.4% | 0.003 | 0.003 | 3 × 5 |
| solve | file=TypeSafe__z3.1184131.smt2 | 0.004 | ± 0.000 | 4.3% | 0.003 | 0.004 | 3 × 5 |
| parse | file=NEQ__NEQ004_size4.smt2 | 0.592 | ± 0.016 | 2.6% | 0.559 | 0.657 | 3 × 5 |
| parse | file=SEQ__SEQ032_size3.smt2 | 0.128 | ± 0.005 | 3.6% | 0.122 | 0.149 | 3 × 5 |
| parse | file=QG-classification__qg5__iso_icl662.smt2 | 1.714 | ± 0.029 | 1.7% | 1.665 | 1.817 | 3 × 5 |
| parse | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 2.963 | ± 0.076 | 2.6% | 2.832 | 3.351 | 3 × 5 |
| parse | file=20190906-CLEARSY__0016__00003.smt2 | 0.145 | ± 0.003 | 1.8% | 0.141 | 0.154 | 3 × 5 |
| princess | file=QG-classification__qg5__iso_icl662.smt2 | 290.438 | ± 7.822 | 2.7% | 274.584 | 320.065 | 3 × 5 |
| princess | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 26298.613 | ± 1578.022 | 6.0% | 23721.280 | 31571.342 | 3 × 5 |
| princess | file=20190906-CLEARSY__0016__00003.smt2 | 19.657 | ± 0.711 | 3.6% | 18.097 | 22.140 | 3 × 5 |
| princess | file=TypeSafe__z3.1184131.smt2 | 1.933 | ± 0.063 | 3.3% | 1.796 | 2.175 | 3 × 5 |
| smtinterpol | file=NEQ__NEQ004_size4.smt2 | 117.696 | ± 3.363 | 2.9% | 112.505 | 129.467 | 3 × 5 |
| smtinterpol | file=SEQ__SEQ032_size3.smt2 | 11.473 | ± 0.405 | 3.5% | 10.710 | 12.934 | 3 × 5 |
| smtinterpol | file=QG-classification__qg5__iso_icl662.smt2 | 17.999 | ± 0.277 | 1.5% | 17.476 | 19.011 | 3 × 5 |
| smtinterpol | file=2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2 | 31.419 | ± 0.969 | 3.1% | 29.400 | 35.019 | 3 × 5 |
| smtinterpol | file=20190906-CLEARSY__0016__00003.smt2 | 1.154 | ± 0.061 | 5.3% | 1.041 | 1.345 | 3 × 5 |

Datatypes (`AdtBench`, the QF_DT instances in process and one process per solver): [2026-08-29-laraserver4-dt.md](2026-08-29-laraserver4-dt.md).

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-29, laraserver4, comparison](2026-08-29-laraserver4-compare.md) — one process per solver and instance, 60 s limit, before and after the simplifier ([tsv](compare-raw.tsv))
- [2026-08-29, laraserver4, theory propagation](2026-08-29-laraserver4-theory.md) — the UF theory scanning all atoms vs. event-driven per-class atom lists (kept) vs. a dirty flag; quick configuration
- [2026-08-29, laraserver4, cached hashes](2026-08-29-laraserver4-hashes.md) — structural hashes of terms and formulas computed once (kept); quick configuration
- [2026-08-29, laraserver4, linear LCA](2026-08-29-laraserver4-lca.md) — the lowest common ancestor in `explain` in linear time (kept); quick configuration
- [2026-08-29, laraserver4](2026-08-29-laraserver4.md) — VIK (`SmtBench
- [2026-08-29, laraserver4, 5 forks](2026-08-29-laraserver4-5forks.json) — `parse`, `solve` and `vik` on the ten instances at 5 forks and 10 warmup iterations (JSON only), before the switch to 3 forks
- [2026-08-29, laraserver4, QF_LIA comparison](2026-08-29-laraserver4-compare-lia.md) — one process per solver and instance on the 14 QF_LIA instances, 60 s limit ([tsv](compare-raw-lia.tsv))
- [2026-08-29, laraserver4, scan](2026-08-29-laraserver4-scan.md) — the event-driven theory propagation reverted to the scan for readability: VIK in process on the 20 instances, before and after
- [2026-08-29, laraserver4, QF_BV](2026-08-29-laraserver4-bv.md) — VIK on the ten QF_BV instances of the arithmetic fragment (3 forks)
- [2026-08-29, laraserver4, QF_BV comparison](2026-08-29-laraserver4-compare-bv.md) — one process per solver and instance on the 12 QF_BV instances, 60 s limit ([tsv](compare-raw-bv.tsv))
- [2026-08-29, laraserver4, in process](2026-08-29-laraserver4-inprocess.md) — VIK, z3 (direct AST and parsed), cvc5, princess and SMTInterpol in the same JVM on the 30 instances
- [2026-08-29, laraserver4, datatypes](2026-08-29-laraserver4-dt.md) — `AdtBench`, the QF_DT instances in process (VIK, z3 direct and parsed, cvc5) and one process per solver ([tsv](compare-raw-dt.tsv))
