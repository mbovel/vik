; From cvc5: regress0/uf/distinct-true.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: unsat
(set-logic QF_UFLIA)
(assert (not (distinct 0 1 2 3 4 5)))
(set-info :status unsat)
(check-sat)
