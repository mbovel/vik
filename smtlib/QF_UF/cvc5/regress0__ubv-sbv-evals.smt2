; From cvc5: regress0/ubv-sbv-evals.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(assert (or 
(not (= (ubv_to_int #b00000000) 0))
(not (= (ubv_to_int #b00001000) 8))
(not (= (sbv_to_int #b00000000) 0))
(not (= (sbv_to_int #b00001000) 8))
(not (= (sbv_to_int #b10001000) (- 120)))
))
(set-info :status unsat)
(check-sat)
