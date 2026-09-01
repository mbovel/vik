; From cvc5: regress0/arith/int-eq-conflict-simple.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic QF_LIRA)
(declare-fun j () Int)
(assert (= 2.5 (to_real j)))
(set-info :status unsat)
(check-sat)
