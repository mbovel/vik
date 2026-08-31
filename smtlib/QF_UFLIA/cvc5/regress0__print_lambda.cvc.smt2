; From cvc5: regress0/print_lambda.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --produce-models
; EXPECT: sat
; EXPECT: (
; EXPECT: (define-fun f ((_arg_1 Int)) Int 0)
; EXPECT: )
(set-logic ALL)
(set-option :incremental false)
(declare-fun f (Int) Int)
(assert (= (f 1) 0))
(set-info :status sat)
(check-sat)
(get-model)
