; Hand-written for VIK (this repository's license): a function applied to `a + 1` and `b + 1`. The graph's
; rows were keyed by the value of the argument, which for a linear term was never computed, so both rows
; sat at 0 and the model gave `f 1 = 2` against the first assertion (issue 24, 2026-09-05).
; EXPECT: sat
; EXPECT: (((f 1) 1) ((f 6) 2) ((f (+ a 1)) 1))
; EXPECT: ((define-fun a () Int 0) (define-fun b () Int 5) (define-fun f ((x!0 Int)) Int (ite (= x!0 6) 2 (ite (= x!0 1) 1 2))))
(set-logic QF_UFLIA)
(set-option :produce-models true)
(declare-fun f (Int) Int)
(declare-const a Int)
(declare-const b Int)
(assert (= (f (+ a 1)) 1))
(assert (= (f (+ b 1)) 2))
(assert (= a 0))
(assert (= b 5))
(set-info :status sat)
(check-sat)
(get-value ((f 1) (f 6) (f (+ a 1))))
(get-model)
