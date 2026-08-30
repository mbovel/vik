; From cvc5: regress0/push-pop/bug1990.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
; EXPECT: unsat
; EXPECT: unsat
(set-logic QF_SAT)
(declare-fun v1 () Bool)
(declare-fun v2 () Bool)
(assert (or v1 v2))
(set-info :status sat)
(check-sat)
(assert false)
(push)
(set-info :status unsat)
(check-sat)
(pop)
(set-info :status unsat)
(check-sat)
