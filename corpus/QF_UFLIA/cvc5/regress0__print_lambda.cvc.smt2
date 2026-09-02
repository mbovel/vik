; From cvc5: regress0/print_lambda.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --produce-models
; EXPECT: sat
; EXPECT: ((define-fun f ((x!0 Int)) Int (ite (= x!0 1) 0 0)))
(set-logic ALL)
(set-option :incremental false)
(declare-fun f (Int) Int)
(assert (= (f 1) 0))
(set-info :status sat)
(check-sat)
(get-model)
