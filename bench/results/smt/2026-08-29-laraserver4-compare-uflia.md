| Instance | Expected | VIK | z3 | cvc5 | princess | SMTInterpol |
|---|---|---:|---:|---:|---:|---:|
| `20230314-Jaroslav-Bendik-Certora__65782_cd31513fdcd15701933b_6_QF_UFLIA.smt2` | sat | 12.77 s | 0.05 s | 0.07 s | 1.84 s | 0.34 s |
| `TwoSquares__smtlib.598294.smt2` | sat | 0.24 s | 0.02 s | 0.01 s | 0.57 s | 0.11 s |
| `TwoSquares__smtlib.602033.smt2` | unsat | 0.22 s | 0.02 s | 0.01 s | 0.49 s | 0.11 s |
| `mathsat__EufLaArithmetic__hard__hard13.smt2` | unsat | timeout | 0.04 s | 0.21 s | 1.89 s | 0.34 s |
| `mathsat__Hash__hash_sat_09_08.smt2` | sat | timeout | 0.03 s | 0.42 s | timeout | 0.20 s |
| `mathsat__Hash__hash_uns_04_18.smt2` | unsat | 1.02 s | 0.02 s | 0.01 s | 0.61 s | 0.15 s |
| `mathsat__Wisa__xs-07-11-1-1-1-1.smt2` | unsat | 0.57 s | 0.03 s | 0.05 s | 1.72 s | 0.21 s |
| `mathsat__Wisa__xs-09-05-2-4-4-5.smt2` | sat | 0.62 s | 0.03 s | 0.01 s | 0.87 s | 0.20 s |
| `wisas__xs_20_20.smt2` | sat | 1.89 s | 0.03 s | 0.02 s | 1.11 s | 0.31 s |
| `wisas__xs_38_38.smt2` | sat | 49.57 s | 0.15 s | 0.03 s | 1.42 s | 1.07 s |
| `wisas__xs_5_5.smt2` | unsat | 0.43 s | 0.02 s | 0.01 s | 0.57 s | 0.15 s |
| `wisas__xs_8_8.smt2` | sat | 0.61 s | 0.02 s | 0.01 s | 0.84 s | 0.18 s |

The Certora file states `:status unknown`; all five solvers answer `sat`, taken as the expected status.
