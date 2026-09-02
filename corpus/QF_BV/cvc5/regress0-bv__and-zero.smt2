; From cvc5: regress0/bv/holes/and-zero.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x1 (_ BitVec 3))
(assert (not (= (bvand x1 #b000) #b000)))
(set-info :status unsat)
(check-sat)
(exit)
