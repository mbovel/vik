# SMT benchmarks

End-to-end benchmarks of the SMT solver (`smt2` parser, `vik` solver, `cdclt`, `egraph`, `lia`) on QF_UF, QF_LIA, QF_BV and QF_DT instances of the
[SMT-LIB](https://smt-lib.org) benchmark library, and a comparison with other solvers.

## Instances

The SMT-LIB instances live in the repository's corpus, [smtlib/](../../../smtlib/README.md), by logic and origin
(`corpus/<logic>/smt-lib/`); the benchmarks use the ones listed in the suites of `io.bovel.bench.evaluation`, the
tests play the whole corpus. The table of families, statuses and licenses is in the corpus README.

## Integer workloads (`UfliaBench`)

Random instances built with the `vik` API:

| Benchmark | Workload | Exercises |
|---|---|---|
| `UfliaBench.solve` | ten sets of `n` random formulas over `n` integer constants in `[-5, 5]` and `f : Int -> Int` (`f(x + 1) <= f(y)`, `f(x) = y - 1`, ...), `n` = 5 and 10 | the combination: shared terms, equality atoms of both theories, disequalities decided by branching; the atoms are quadratic in the shared terms (`n` = 20 takes minutes per set) |

## Running

From the repository root (see [bench/README.md](../../README.md) for the setup of `jmh2md.py`):

```sh
sbt "bench/Jmh/run -rf json -rff target/jmh-result.json .*VikBench.*"   # VIK on every instance of the evaluation
sbt "bench/Jmh/run -p file=eq_diamond__eq_diamond50.smt2"     # one instance
bench/.venv/bin/python bench/jmh2md.py target/jmh-result.json
bench/cold.sh bench/evaluation-instances.txt > bench/results/smt/cold.tsv   # wall clock vs z3, cvc5, princess, SMTInterpol
```

The other benchmarks are recorded on subsets (`-p file=...`): `parse` and `smtinterpol` on a few representative instances,
`princess` on those it solves in seconds (it needs minutes on NEQ004, PEQ018 and SEQ032).

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

In process, the suites of `io.bovel.bench.evaluation` run princess and SMTInterpol (JVM libraries) and z3 and cvc5 through their Java bindings
as published by SoSy-Lab (the JavaSMT project) on Maven Central: the Java parts are dependencies of `bench`, the native
libraries are downloaded by `bench/fetch-native.sh` into `bench/native/` (Linux x86-64) and found through
`java.library.path`. `Z3TextBench.text` has z3 parse the script, `Z3Bench.api` builds z3's terms through the API (the
counterpart of `VikBench.api`). `sbt "bench/runMain io.bovel.bench.evaluation.SolverCheck"` runs every in-process
solver on every instance and reports disagreements with the expected status.

`cold.sh` runs each solver as a separate process on the instances of a file list, with a timeout, and records the
answer and the wall clock over repetitions, JVM start-up included for VIK, princess and SMTInterpol. z3 and cvc5 are
taken from the `PATH`; princess and SMTInterpol are fetched with [coursier](https://get-coursier.io).

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
- [2026-08-29, laraserver4, integers](2026-08-29-laraserver4-uflia.md) — `UfliaBench` (n = 5: 1.7 ms, n = 10: 200 ms)
- [2026-08-29, laraserver4, QF_UFLIA comparison](2026-08-29-laraserver4-compare-uflia.md) — one process per solver and instance on the 12 QF_UFLIA instances, 60 s limit ([tsv](compare-raw-uflia.tsv)): VIK 9/12 in 0.2–1.9 s (`xs_38_38` 50 s), z3 and cvc5 0.01–0.4 s, princess 0.5–1.9 s, SMTInterpol 0.1–1.1 s; `hash_sat_09_08` and `hard13` exceed 60 s
- [2026-08-29, laraserver4, QF_UFLIA instances](2026-08-29-laraserver4-uflia-instances.md) — VIK in process on the eight QF_UFLIA instances of the JMH set (wisas `xs_20_20` 0.73 s, Hash `hash_uns_04_18` 0.37 s)
- [2026-09-03, laraserver4, the bytecode optimizer](2026-09-03-laraserver4-opt.md) — Scala 3.9's `-opt` on the JVM rows, cold (not kept): every column within noise of the run without it; cold is start-up and JIT, warm the JIT does these optimizations itself
- [2026-09-03, laraserver4, the compiler's bit-vector fuel-outs](2026-09-03-laraserver4-hash-consing.md) — one chain of fresh constants per bit-vector operation and level (kept): 30 of the 31 queries the checker's comparison had VIK give up on answer in tens of milliseconds; two `sat` files fall to the simplex walking 32-bit ranges, the remaining cause
- [2026-09-03, laraserver4, bits as cases](2026-09-03-laraserver4-bits-as-cases.md) — the sign as the atom `a ≥ 2ⁿ⁻¹` and the overflow as a disjunction of equalities, no simplex unknown with `2ⁿ` as its coefficient (kept): all 31 of the compiler's fuel-outs answer, `sage` 22× and `pspace` 4× faster warm on top of the hash-consing
- [2026-09-03, laraserver4, the disequality clause](2026-09-03-laraserver4-disequality-clause.md) — a false pair atom as a clause over two bound atoms, the relaxation's phase at their first decision (kept): `wisas` 2×, `hash` 3×, Certora 2.4× cold, `xs_8_8` 3× warm; the compiler's medium queries 3× slower, its hardest 2× faster, total +19% — the lazy side (atoms during the search) is the fix
- [2026-09-04, laraserver4, the atoms of a popped level](2026-09-04-laraserver4-retired-atoms.md) — a level's atoms left out of the decisions after its pop (not kept): thirty times faster on a script that pushes and pops a bit-vector query, and unsound as written, since the theory lemmas learned under the level outlive it and name its atoms; deleting those learned clauses first is the fix, and needs clause deletion in `cdclt`
- [2026-09-04, laraserver4, the day's evaluation](2026-09-04-laraserver4-warm.json) — warm and cold after the bit-vector fragment and the per-check fixes: VIK 0.77× its time of the day before over the shared instances (`wisas__xs_8_8` 0.23×, `mathsat__Wisa` 0.50×, `safari__mux` 0.57×, `kind__car_6` 0.67×), two instances slower (`RandomDecoupled__pb_real` 3.2×, `bmc__stack_k20` 1.9×), and the two bit-vector instances of the day measured for the first time
- [2026-09-05, laraserver4, reals separated by a strict bound](2026-09-05-laraserver4-reals.md) — the fix for issues 7 and 8 (kept): one cold instance 1.5× slower, the other real instances flat, and the implication between the two bounds of the clause worth 1.9× of the cost it removes
- [2026-09-05, laraserver4, state decided at the first solve](2026-09-05-laraserver4-first-solve-state.md) — the fix for issues 13 and 14 (kept): every uninterpreted-function, array and datatype instance of the evaluation within a hundredth of a second of the committed run
- [2026-09-05, laraserver4, names and scopes in the front end](2026-09-05-laraserver4-names-and-scopes.md) — the fix for issues 10 and 11 (kept): the declaration-heavy instances flat
- [2026-09-05, laraserver4, the zero quotient of a signed division](2026-09-05-laraserver4-signed-division-zero.md) — the fix for issue 9 (kept, `q ≥ 1` in the condition): the bit-vector instances flat, and one release file out of fuel that had answered on the back of the bug
