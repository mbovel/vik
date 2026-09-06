; Hand-written for VIK (this repository's license): the value of a function applied to a store is the
; row the script asserted, arrays compared as arrays (issue 49): VIK compared the arguments as terms,
; missed the row keyed by the store read back, and answered the default against its own assertion.
; EXPECT: sat
; EXPECT: (((f (store a 0 1)) 7) ((f a) 3) ((f b) 7) ((f (store (store a 0 1) 0 1)) 7))
(set-logic QF_AUFLIA)
(set-option :produce-models true)
(declare-const a (Array Int Int))
(declare-const b (Array Int Int))
(declare-fun f ((Array Int Int)) Int)
(assert (= (f (store a 0 1)) 7))
(assert (= (f a) 3))
(assert (= b (store a 0 1)))
(set-info :status sat)
(check-sat)
(get-value ((f (store a 0 1)) (f a) (f b) (f (store (store a 0 1) 0 1))))
