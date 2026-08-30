; From cvc5: regress0/bv/divtest_2_6.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)
(declare-fun x () (_ BitVec 8))
(declare-fun y () (_ BitVec 8))

(assert (not (= (bvudiv x (_ bv0 8)) (_ bv255 8))))
(set-info :status unsat)
(check-sat)
