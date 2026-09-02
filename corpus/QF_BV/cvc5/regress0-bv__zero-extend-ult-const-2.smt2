; From cvc5: regress0/bv/holes/zero-extend-ult-const-2.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 3))
(assert (not (=
	(bvult #b000110 ((_ zero_extend 3) x))
	(bvult #b110 x)
	)))
(set-info :status unsat)
(check-sat)
(exit)
