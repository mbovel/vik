; From cvc5: regress0/bv/holes/sign-extend-ult-const-1.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 3))
(assert (not (=
	(bvult ((_ sign_extend 3) x) #b000010)
	(bvult x #b010)
	)))
(set-info :status unsat)
(check-sat)
(exit)
