; From cvc5: regress0/bv/holes/mult-distrib-const-neg.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :status unsat)

(declare-const x (_ BitVec 4))
(assert (not (=
	(bvmul (bvneg x) #b0100)
	(bvmul x #b1100))))
(set-info :status unsat)
(check-sat)
(exit)
