; From cvc5: regress0/smtlib/reason-unknown.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: (error "Can't get-info :reason-unknown when the last result wasn't unknown!")
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for QF_SAT (prune logic)
(set-logic QF_UF)
(get-info :reason-unknown)
(set-info :status sat)
(check-sat)
