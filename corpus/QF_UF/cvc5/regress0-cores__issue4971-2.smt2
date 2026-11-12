; From cvc5: regress0/cores/issue4971-2.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental --produce-unsat-cores
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for QF_SLIA (prune logic)
(set-logic QF_UF)
(assert false)
(reset-assertions)
(set-info :status sat)
(check-sat)
