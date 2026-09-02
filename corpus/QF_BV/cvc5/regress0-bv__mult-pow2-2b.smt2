; From cvc5: regress0/bv/holes/mult-pow2-2b.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const z (_ BitVec 4))
(assert (not (=
  (bvmul z #b1100)
  (concat
    ((_ extract 1 0) (bvneg z))
    #b00)
	)))
(set-info :status unsat)
(check-sat)
(exit)
