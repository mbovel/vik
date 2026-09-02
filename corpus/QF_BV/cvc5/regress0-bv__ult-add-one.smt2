; From cvc5: regress0/bv/holes/ult-add-one.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 5))
(declare-const y (_ BitVec 5))
(assert (not (=
	(bvult x (bvadd y #b00001))
	(and
		(not (bvult y x))
		(not (= y #b11111))
	))))
(set-info :status unsat)
(check-sat)
(exit)
