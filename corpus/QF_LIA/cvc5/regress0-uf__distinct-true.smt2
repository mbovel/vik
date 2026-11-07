; From cvc5: regress0/uf/distinct-true.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
; logic: QF_LIA, the smallest the script fits, for QF_UFLIA (prune logic)
(set-logic QF_LIA)
(assert (not (distinct 0 1 2 3 4 5)))
(set-info :status unsat)
(check-sat)
