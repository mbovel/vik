; From cvc5: regress0/bv/holes/not-sle.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 4))
(declare-const y (_ BitVec 4))
(assert (not (=
  (not (bvsle x y))
  (bvslt y x)
	)))
(set-info :status unsat)
(check-sat)
(exit)
