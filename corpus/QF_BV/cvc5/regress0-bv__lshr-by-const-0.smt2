; From cvc5: regress0/bv/holes/lshr-by-const-0.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 5))
(assert (not (=
  (bvlshr x #b00000)
	x
	)))
(set-info :status unsat)
(check-sat)
(exit)
