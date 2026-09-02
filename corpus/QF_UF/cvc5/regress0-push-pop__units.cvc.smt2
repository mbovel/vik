; From cvc5: regress0/push-pop/units.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
; EXPECT: sat
; EXPECT: unsat
; EXPECT: sat
; EXPECT: sat
; EXPECT: unsat
; EXPECT: sat
(set-logic ALL)
(declare-fun x () Bool)
(declare-fun y () Bool)
(assert (or x y))
(set-info :status sat)
(check-sat)
(push 1)
(assert (not x))
(set-info :status sat)
(check-sat)
(push 1)
(assert (not y))
(set-info :status unsat)
(check-sat)
(pop 1)
(set-info :status sat)
(check-sat)
(pop 1)
(set-info :status sat)
(check-sat)
(push 1)
(push 1)
(assert x)
(assert (not x))
(set-info :status unsat)
(check-sat)
(pop 1)
(pop 1)
(set-info :status sat)
(check-sat)
