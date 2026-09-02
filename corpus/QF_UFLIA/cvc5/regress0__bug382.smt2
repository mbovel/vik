; From cvc5: regress0/bug382.smt2 (modified BSD, LICENSES/cvc5.txt)
; EXPECT: sat
; EXPECT: ((x 0))
; EXPECT: ((x 0))
; EXPECT: (((f x) 0))
; EXPECT: (((f x) 0))
(set-option :produce-models true)
(set-logic QF_UFLIA)
(declare-fun f (Int) Int)
(declare-fun x () Int)
(set-info :status sat)
(check-sat)
(get-value (x)); returns 0
(get-value (x)); returns 1 ?!
(get-value ((f x))); assert-fails in EqualityEngine
(get-value ((f x)))
