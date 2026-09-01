; From cvc5: regress0/dd_arith_bv_conv-rpf.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(declare-fun y () Int)
(assert (or false (and (distinct 0 0) (bvule ((_ int_to_bv 1) y) (_ bv0 1)))))
(set-info :status unsat)
(check-sat)
