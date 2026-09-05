; Hand-written for VIK (this repository's license): an array read at `i + 1`. The model filed the read at
; index 0, the value it gave a linear index, and answered 0 for a read the assertion sets to 1 (issue 25,
; 2026-09-05).
; EXPECT: sat
; EXPECT: (((select a (+ i 1)) 1) ((select a 6) 1))
; EXPECT: ((define-fun a () (Array Int Int) (store ((as const (Array Int Int)) 0) 6 1)) (define-fun i () Int 5))
(set-logic QF_ALIA)
(set-option :produce-models true)
(declare-const a (Array Int Int))
(declare-const i Int)
(assert (= i 5))
(assert (= (select a (+ i 1)) 1))
(set-info :status sat)
(check-sat)
(get-value ((select a (+ i 1)) (select a 6)))
(get-model)
