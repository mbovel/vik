# SMT benchmarks

End-to-end benchmarks of the SMT solver (`smt2` parser, `smt` solver, `cdclt`, `egraph`) on QF_UF instances of the
[SMT-LIB](https://smt-lib.org) benchmark library, and a comparison with other solvers.

## Instances

From the [SMT-LIB release 2025 (non-incremental benchmarks)](https://zenodo.org/records/16740866), `QF_UF.tar.zst`,
copied to [src/main/resources/smtlib](src/main/resources/smtlib) with the family as a prefix (`family__file.smt2`).
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

Licenses, as stated in the files: 2018-Goel-hwbench [CC BY 4.0](https://creativecommons.org/licenses/by/4.0/);
20170829-Rodin and 20190906-CLEARSY [CC BY-NC 4.0](https://creativecommons.org/licenses/by-nc/4.0/); the other
families state a `:source` only. See each file's `set-info` header for the authors.

## Running

From the repository root (see [egraph-bench/README.md](../egraph-bench/README.md) for the setup of `jmh2md.py`):

```sh
sbt "smtBench/Jmh/run -rf json -rff target/jmh-result.json .*SmtBench.solve .*OtherSolversBench.vik"   # VIK on every instance
sbt "smtBench/Jmh/run -p file=eq_diamond__eq_diamond50.smt2"     # one instance
egraph-bench/.venv/bin/python egraph-bench/jmh2md.py target/jmh-result.json
smt-bench/compare.sh 60 > smt-bench/results/compare.tsv           # wall clock vs z3, cvc5, princess, SMTInterpol
```

The other benchmarks are recorded on subsets (`-p file=...`): `parse` and `smtinterpol` on a few representative instances,
`princess` on those it solves in seconds (it needs minutes on NEQ004, PEQ018 and SEQ032).

Configuration (annotations in the benchmark class): 3 forks, 5 warmup and 5 measurement iterations of 1 s each, average time per operation, which gives 95% confidence intervals of a few percent; `-f 5 -wi 10` on the command line for a closer call.

`compare.sh` runs each solver as a separate process with a timeout (default 60 s) and records the answer and the wall
clock, JVM start-up included for VIK, princess and SMTInterpol. z3 and cvc5 are taken from the `PATH`; princess and
SMTInterpol are fetched with [coursier](https://get-coursier.io).

## Current results

Process-level comparison with z3, cvc5, princess and SMTInterpol, and the effect of the simplifier: [results/2026-08-29-laraserver4-compare.md](results/2026-08-29-laraserver4-compare.md). JMH results (in-process) to come.

## All results

One file per run, named by date and host (plus a tag when a day has several runs), with the raw JMH JSON next to it:
- [2026-08-29, laraserver4, comparison](results/2026-08-29-laraserver4-compare.md) — one process per solver and instance, 60 s limit, before and after the simplifier ([tsv](results/compare-raw.tsv))
- [2026-08-29, laraserver4, theory propagation](results/2026-08-29-laraserver4-theory.md) — the UF theory scanning all atoms vs. event-driven per-class atom lists (kept) vs. a dirty flag; quick configuration
- [2026-08-29, laraserver4, cached hashes](results/2026-08-29-laraserver4-hashes.md) — structural hashes of terms and formulas computed once (kept); quick configuration
- [2026-08-29, laraserver4, linear LCA](results/2026-08-29-laraserver4-lca.md) — the lowest common ancestor in `explain` in linear time (kept); quick configuration
