; From cvc5: regress0/bv/div-zero-eval-tests.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)

(assert (or

(not (= (bvudiv #b1010 #b0000) #b1111))
(not (= (bvurem #b1010 #b0000) #b1010))

))
(set-info :status unsat)
(check-sat)
