; From cvc5: regress0/datatypes/boolean-terms-datatype.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((List 0)) (((cons (car Bool) (cdr List)) (nil))))
(declare-fun x () List)
(assert (not (= x nil)))
(set-info :status sat)
(check-sat)
