; From cvc5: regress0/smtlib/issue4028.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(set-option :incremental true)
(push 3)
(set-info :status sat)
(check-sat)
(reset-assertions)
(push 4)
(set-info :status sat)
(check-sat)
