; From cvc5: regress0/cores/issue4971-2.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental --produce-unsat-cores
; EXPECT: sat
(set-logic QF_SLIA)
(assert false)
(reset-assertions)
(set-info :status sat)
(check-sat)
