# Solver comparison, 2026-08-29, laraserver4

Wall clock of one process per solver and instance, JVM start-up included for VIK, princess and SMTInterpol
(`bench/compare.sh 60`, raw data in [compare-raw.tsv](compare-raw.tsv)). Answers all agree with the expected
status. The "before simplifier" column is the same protocol on VIK before the local-constant elimination of
`smt.Simplifier` (commit `22e294e`), measured by hand with the same 60 s limit.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- VIK: this repository, OpenJDK 17.0.20. z3 4.13.0 and cvc5 1.1.2 (native, from the `PATH`); princess 2026-05-20 and
  SMTInterpol 2.5-916 (JVM, from Maven Central through sbt), each started with `java -cp` on the same classpath.

| Instance | Expected | VIK before simplifier | VIK | z3 | cvc5 | princess | SMTInterpol |
|---|---|---:|---:|---:|---:|---:|---:|
| `20170829-Rodin__smt3534838651727683253.smt2` | unsat | 0.18 s | 0.19 s | 0.02 s | 0.01 s | 0.49 s | 0.10 s |
| `2018-Goel-hwbench__QF_UF_bug-1_ab_reg_max.smt2` | sat | 0.19 s | 0.19 s | 0.02 s | 0.01 s | 0.51 s | 0.10 s |
| `2018-Goel-hwbench__QF_UF_mcs.1.prop1_ab_cti_max.smt2` | sat | 0.33 s | 0.34 s | 0.03 s | 0.03 s | 2.25 s | 0.22 s |
| `20190906-CLEARSY__0016__00003.smt2` | sat | 0.21 s | 0.20 s | 0.02 s | 0.01 s | 0.61 s | 0.10 s |
| `NEQ__NEQ004_size4.smt2` | unsat | 0.63 s | 0.67 s | 0.02 s | 0.04 s | timeout | 0.36 s |
| `PEQ__PEQ018_size4.smt2` | unsat | 0.62 s | 0.64 s | 0.06 s | 0.21 s | timeout | 0.42 s |
| `QG-classification__qg5__iso_brn001.smt2` | sat | 0.26 s | 0.29 s | 0.02 s | 0.01 s | 0.90 s | 0.15 s |
| `QG-classification__qg5__iso_icl662.smt2` | unsat | 0.34 s | 0.35 s | 0.02 s | 0.02 s | 1.50 s | 0.20 s |
| `SEQ__SEQ009_size10.smt2` | unsat | timeout | timeout | 0.02 s | 5.29 s | timeout | timeout |
| `SEQ__SEQ032_size3.smt2` | unsat | 0.44 s | 0.41 s | 0.02 s | 0.02 s | 29.70 s | 0.17 s |
| `TypeSafe__z3.1184131.smt2` | unsat | 0.20 s | 0.20 s | 0.02 s | 0.01 s | 0.50 s | 0.10 s |
| `eq_diamond__eq_diamond20.smt2` | unsat | timeout | 0.21 s | 0.02 s | 0.01 s | 14.10 s | 22.74 s |
| `eq_diamond__eq_diamond50.smt2` | unsat | timeout | 0.22 s | 0.02 s | 0.01 s | timeout | timeout |

- The simplifier turns the `eq_diamond` instances from timeouts into 0.2 s: each diamond collapses to one equality.
- `SEQ009_size10` remains out of reach for VIK (and for princess and SMTInterpol); cvc5 needs 5 s, z3 0.02 s.
- On the other ten instances VIK takes 0.2–0.7 s, most of it JVM start-up and class loading (see the JMH numbers
  for the in-process cost); z3 and cvc5 take 0.01–0.2 s; princess 0.5–30 s or timeouts; SMTInterpol 0.1–0.4 s.
