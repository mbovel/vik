; From cvc5: regress0/push-pop/test.01.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(declare-fun x () Bool)
(declare-fun y () Bool)
(assert (or x y))
(set-info :status sat)
(check-sat)
(push 1)
(assert (not x))
(set-info :status sat)
(check-sat)
(pop 1)
(push 1)
(assert (not y))
(set-info :status sat)
(check-sat)
(pop 1)
(set-info :status sat)
(check-sat)
