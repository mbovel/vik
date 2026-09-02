; From cvc5: regress0/printer/get-value-no-letify.smt2 (modified BSD, LICENSES/cvc5.txt)
; COMMAND-LINE: --produce-models
; EXPECT: sat
; EXPECT: ((x (data 0)) (y (data 0)))
(set-logic ALL)
(declare-datatype D ((data (sel Int))))
(declare-fun x () D)
(declare-fun y () D)
(assert (= x y (data 0)))
(set-info :status sat)
(check-sat)
(get-value (x y))
