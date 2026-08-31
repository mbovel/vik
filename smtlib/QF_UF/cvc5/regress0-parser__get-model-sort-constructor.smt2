; From cvc5: regress0/parser/get-model-sort-constructor.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; EXPECT: (
; EXPECT: )
(set-option :produce-models true)
(set-logic QF_UFLIRA)
(declare-sort FArray 2)
(set-info :status sat)
(check-sat)
(get-model)
