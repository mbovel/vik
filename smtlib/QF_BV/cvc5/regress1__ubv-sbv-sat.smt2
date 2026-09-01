; From cvc5: regress1/ubv-sbv-sat.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(declare-fun x () (_ BitVec 8))
(declare-fun y () (_ BitVec 8))
(assert (= (ubv_to_int x) (sbv_to_int y)))
(set-info :status sat)
(check-sat)
