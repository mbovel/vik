; From cvc5: regress0/preprocess/proj-issue684.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(assert false)
(assert (xor false (not (not (not false)))))
(set-info :status unsat)
(check-sat)
