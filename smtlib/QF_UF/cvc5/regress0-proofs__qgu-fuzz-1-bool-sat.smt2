; From cvc5: regress0/proofs/qgu-fuzz-1-bool-sat.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic QF_UF)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(assert (and (or d b) (= c d) (not (ite d c false)) (= (or b d) (= b d))))
(set-info :status unsat)
(check-sat)