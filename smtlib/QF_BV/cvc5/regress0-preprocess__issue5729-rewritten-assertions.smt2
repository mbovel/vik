; From cvc5: regress0/preprocess/issue5729-rewritten-assertions.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic QF_ALL)
(declare-fun x () (_ BitVec 1))
(assert (= (_ bv0 1) ((_ int_to_bv 1) (ubv_to_int x))))
(set-info :status sat)
(check-sat)
