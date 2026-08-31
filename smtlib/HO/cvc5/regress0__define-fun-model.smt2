; From cvc5: regress0/define-fun-model.smt2 (modified BSD, LICENSES/cvc5.txt)
; SCRUBBER: sed -e 's/BOUND_VARIABLE_[0-9]*/V/; s/_arg_[0-9]*/V/'
; EXPECT: sat
; EXPECT: (((f 4) 7))
; EXPECT: ((g g))
(set-logic UFLIA)
(set-option :produce-models true)
(define-fun f ((x Int)) Int 7)
(declare-fun g (Int) Int)

(assert (= (g 5) (f 5)))

(set-info :status sat)
(check-sat)
(get-value ((f 4)))
(get-value (g))
(get-value (f))
