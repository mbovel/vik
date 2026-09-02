; From cvc5: regress0/uflia/check01.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
(set-logic QF_UFLIA)
(set-info :smt-lib-version 2.6)
(set-info :status sat)

(declare-fun f (Int) Bool)

(assert (f 0))

(push 1)
(assert (not (f 0)))
(pop 1)
(set-info :status sat)
(check-sat)
