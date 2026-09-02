; From cvc5: regress0/datatypes/boolean-terms-rewrite.cvc.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
(set-logic ALL)
(set-option :incremental false)
(declare-datatypes ((|__cvc5_record_b_(_ BitVec 1)| 0)) (((|__cvc5_record_b_(_ BitVec 1)_ctor| (b (_ BitVec 1))))))
(declare-fun x () |__cvc5_record_b_(_ BitVec 1)|)
(assert (= false (= (= x (|__cvc5_record_b_(_ BitVec 1)_ctor| #b0)) (= x (|__cvc5_record_b_(_ BitVec 1)_ctor| #b1)))))
(set-info :status sat)
(check-sat)
