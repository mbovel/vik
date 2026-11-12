; From cvc5: regress0/print_lambda.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --produce-models
; EXPECT: sat
; EXPECT: ((define-fun f ((x!0 Int)) Int (ite (= x!0 1) 0 0)))
; logic: QF_UFLIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_UFLIA)
(set-option :incremental false)
(declare-fun f (Int) Int)
(assert (= (f 1) 0))
(set-info :status sat)
(check-sat)
(get-model)
