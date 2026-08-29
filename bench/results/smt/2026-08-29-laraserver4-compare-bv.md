# Solver comparison on QF_BV, 2026-08-29, laraserver4

Wall clock of one process per solver and instance, JVM start-up included for VIK and princess
(`bench/compare.sh 60 'sage__|bench_ab|...'`, raw data in [compare-raw-bv.tsv](compare-raw-bv.tsv)). VIK lowers the
arithmetic fragment of bit-vectors to linear integer arithmetic (no bit-blasting); z3 and cvc5 bit-blast. SMTInterpol
has no bit-vector support (it answers `sat` regardless) and is omitted.

- Host: laraserver4, AMD EPYC 9254 (24 cores / 96 threads), shared with other users.
- VIK: this repository, OpenJDK 17.0.20. z3 4.13.0 and cvc5 1.1.2 (native, from the `PATH`); princess 2026-05-20
  (JVM, from Maven Central through sbt), started with `java -cp` on the same classpath.

| Instance | Expected | VIK | z3 | cvc5 | princess |
|---|---|---:|---:|---:|---:|
| `2018-Goel-hwbench__QF_BV_bv_bv_swap_two_cc_ref_max.smt2` | sat | 0.27 s | 0.02 s | 0.01 s | 0.57 s |
| `2018-Goel-hwbench__QF_BV_cav14_example_cc_reg_max.smt2` | unsat | 0.28 s | 0.02 s | 0.01 s | 0.58 s |
| `20190311-bv-term-small-rw-Noetzli__bv-term-small-rw_17.smt2` | unsat | 0.21 s | 0.01 s | 0.01 s | 0.48 s |
| `bench_ab__a97test0001.smt2` | sat | 0.21 s | 0.02 s | 0.01 s | 0.52 s |
| `bmc-bv__Prim_4.smt2` | unsat | 0.20 s | 0.02 s | 0.01 s | 0.46 s |
| `bmc-bv__adpcm.smt2` | sat | 0.19 s | 0.02 s | 0.01 s | 0.45 s |
| `bruttomesso__core__ext_con_004_001_0016.smt2` | unsat | timeout | 0.02 s | 0.01 s | 0.82 s |
| `crafted__bitops7.smt2` | sat | 0.24 s | 0.02 s | 0.01 s | 0.67 s |
| `crafted__bv8.smt2` | unsat | 0.22 s | 0.01 s | 0.01 s | 0.48 s |
| `fft__Sz128_2823.smt2` | unsat | timeout | 0.06 s | timeout | timeout |
| `pspace__ndist.a.20000.smt2` | unsat | 0.24 s | timeout | 0.01 s | 0.58 s |
| `sage__app7__bench_8505.smt2` | sat | 0.21 s | 0.02 s | 0.01 s | 0.52 s |

- VIK answers 10 of the 12 within 60 s, all in 0.2–0.3 s (JVM start-up included; 1 µs to 0.3 ms in process, see
  [2026-08-29-laraserver4-bv.md](2026-08-29-laraserver4-bv.md)).
- `pspace/ndist` (wide bit-vectors): 0.24 s for VIK and 0.01 s for cvc5, a 20 s timeout for z3.
- `fft/Sz128`: z3 in 0.06 s, timeouts for VIK, cvc5 and princess; `bruttomesso/ext_con` (chains of `extract` and
  `concat`, each a division in the encoding): a timeout for VIK, instant for the bit-blasters.
