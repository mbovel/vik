; From cvc5: regress0/arith/int-geq-tighten-simple.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(declare-fun i () Int)
(assert (>= (to_real i) 2.5))
(assert (= i 0))
(set-info :status unsat)
(check-sat)
