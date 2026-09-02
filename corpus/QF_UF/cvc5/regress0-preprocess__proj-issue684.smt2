; From cvc5: regress0/preprocess/proj-issue684.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(assert false)
(assert (xor false (not (not (not false)))))
(set-info :status unsat)
(check-sat)
