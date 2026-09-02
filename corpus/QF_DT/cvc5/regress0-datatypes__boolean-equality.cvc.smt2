; From cvc5: regress0/datatypes/boolean-equality.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((__cvc5_record_b_Bool 0)) (((__cvc5_record_b_Bool_ctor (b Bool)))))
(declare-fun x () __cvc5_record_b_Bool)
(assert (= x x))
(set-info :status sat)
(check-sat)
