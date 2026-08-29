# Solver comparison on QF_LIA, 2026-08-29, laraserver4

Wall clock of one process per solver and instance, JVM start-up included for VIK, princess and SMTInterpol
(`smt-bench/compare.sh 60 'CAV_2009|CIRC|...'`, raw data in [compare-raw-lia.tsv](compare-raw-lia.tsv)), VIK with the
LIA theory (bound propagation with one row visit per propagation, branch and bound in the final check).

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- VIK: this repository, OpenJDK 17.0.20. z3 4.13.0 and cvc5 1.1.2 (native, from the `PATH`); princess 2026-05-20 and
  SMTInterpol 2.5-916 (JVM, from Maven Central through sbt), each started with `java -cp` on the same classpath.

| Instance | Expected | VIK | z3 | cvc5 | princess | SMTInterpol |
|---|---|---:|---:|---:|---:|---:|
| `CAV_2009_benchmarks__20-vars__problem__001.smt2` | sat | 0.28 s | 0.02 s | 0.03 s | 0.57 s | 0.13 s |
| `CIRC__multiplier_prime__MULTIPLIER_PRIME_2.msat.smt2` | sat | 0.27 s | 0.02 s | 0.01 s | 0.57 s | 0.11 s |
| `calypto__problem-005675.cvc.2.smt2` | sat | 0.31 s | 0.02 s | 0.01 s | 1.62 s | 0.35 s |
| `convert__convert-tiff2jpg-query-1831.smt2` | unsat | 0.28 s | 0.02 s | 0.01 s | 0.55 s | 0.14 s |
| `cut_lemmas__10-vars__cut_lemma_01_001.smt2` | unsat | 0.22 s | 0.02 s | 0.01 s | 0.48 s | 0.11 s |
| `cut_lemmas__20-vars__cut_lemma_01_001.smt2` | unsat | 5.15 s | timeout | timeout | **wrong** 7.17 s | 15.55 s |
| `dillig__20-4.smt2` | sat | 0.31 s | 0.02 s | 0.03 s | 0.59 s | 0.13 s |
| `fft__Sz64_1160.smt2` | sat | 0.70 s | 0.03 s | 0.02 s | 1.64 s | 0.18 s |
| `mathsat__FISCHER1-1-fair.smt2` | sat | 0.27 s | 0.02 s | 0.01 s | 0.61 s | 0.12 s |
| `nec-smt__small__int_from_list__prp-2-17.smt2` | unsat | 1.37 s | 0.02 s | 0.03 s | 1.05 s | 0.17 s |
| `pidgeons__pigeon-hole-6.smt2` | unsat | 0.25 s | 0.02 s | 0.01 s | 0.55 s | 0.13 s |
| `prime-cone__prime_cone_unsat_3.smt2` | unsat | 0.22 s | 0.02 s | 0.01 s | 0.49 s | 0.11 s |
| `rings__ring_2exp4_3vars_2ite_unsat.smt2` | unsat | 0.51 s | 0.03 s | 0.03 s | 0.86 s | 0.15 s |
| `wisa__wisa1.smt2` | sat | timeout | 0.03 s | 0.05 s | 1.58 s | 0.91 s |

- VIK answers 13 of the 14 within 60 s, 10 of them in 0.2–0.6 s (JVM start-up included), `fft` and `nec-smt` in
  0.7 and 1.4 s (17 s and a timeout before bound propagation), `cut_lemmas/20` in 5 s. `wisa1` is out of reach.
- On `cut_lemmas/20` z3 and cvc5 time out and princess answers `sat` for an instance whose `:status` is `unsat`
  (VIK and SMTInterpol agree with the status); the family is designed to need cuts or search.
- z3 and cvc5 take 0.01–0.05 s elsewhere; princess 0.5–1.6 s; SMTInterpol 0.1–0.9 s.
