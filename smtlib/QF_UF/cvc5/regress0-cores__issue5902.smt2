; From cvc5: regress0/cores/issue5902.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic ALL)
(set-info :status unsat)
(check-sat-assuming (false))
