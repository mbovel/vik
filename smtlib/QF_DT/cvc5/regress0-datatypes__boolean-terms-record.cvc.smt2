; From cvc5: regress0/datatypes/boolean-terms-record.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((__cvc5_record_i_Int_b_Bool 0)) (((__cvc5_record_i_Int_b_Bool_ctor (i Int) (b Bool)))))
(declare-fun x () __cvc5_record_i_Int_b_Bool)
(assert (not (= x (__cvc5_record_i_Int_b_Bool_ctor 0 false))))
(set-info :status sat)
(check-sat)
