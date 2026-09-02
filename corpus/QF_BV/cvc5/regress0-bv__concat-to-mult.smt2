; From cvc5: regress0/bv/holes/concat-to-mult.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 10))
(declare-const y (_ BitVec 5))
(assert (not (=
	(concat ((_ extract 4 0) x) #b00000)
	(bvmul x (bvshl #b0000000001 #b0000000101)))))
(set-info :status unsat)
(check-sat)
(exit)
