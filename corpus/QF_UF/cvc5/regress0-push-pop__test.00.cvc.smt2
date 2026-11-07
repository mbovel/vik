; From cvc5: regress0/push-pop/test.00.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --incremental
; EXPECT: sat
; EXPECT: sat
; logic: QF_UF, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UF)
(declare-fun x () Bool)
(push 1)
(assert x)
(set-info :status sat)
(check-sat)
(pop 1)
(assert (not x))
(set-info :status sat)
(check-sat)
