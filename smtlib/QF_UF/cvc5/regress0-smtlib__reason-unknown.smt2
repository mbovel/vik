; From cvc5: regress0/smtlib/reason-unknown.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: (error "Can't get-info :reason-unknown when the last result wasn't unknown!")
; EXPECT: sat
(set-logic QF_SAT)
(get-info :reason-unknown)
(set-info :status sat)
(check-sat)
