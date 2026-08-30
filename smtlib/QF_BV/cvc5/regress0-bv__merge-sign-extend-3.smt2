; From cvc5: regress0/bv/holes/merge-sign-extend-3.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 10))
(assert (not (=
	((_ sign_extend 10) ((_ zero_extend 0) x))
	((_ sign_extend 10) x))))
(set-info :status unsat)
(check-sat)
(exit)
