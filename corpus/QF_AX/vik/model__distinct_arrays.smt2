; Hand-written for VIK (this repository's license): two arrays asserted distinct
; get values that differ at the witness index the solver introduced, and an
; array equal to a store on another gets that store's write.
; EXPECT: sat
; EXPECT: (((= a c) false) ((= a b) false) ((select c i) @E!0))
; EXPECT: ((define-fun a () (Array I E) (store ((as const (Array I E)) @E!1) @I!0 @E!0)) (define-fun b () (Array I E) (store ((as const (Array I E)) @E!3) @I!0 @E!2)) (define-fun c () (Array I E) (store ((as const (Array I E)) @E!4) @I!0 @E!0)) (define-fun i () I @I!0) (define-fun v () E @E!0))
(set-logic QF_AX)
(set-option :produce-models true)
(declare-sort I 0)
(declare-sort E 0)
(declare-const a (Array I E))
(declare-const b (Array I E))
(declare-const c (Array I E))
(declare-const i I)
(declare-const v E)
(assert (distinct a b))
(assert (= (select a i) v))
(assert (= c (store a i v)))
(set-info :status sat)
(check-sat)
(get-value ((= a c) (= a b) (select c i)))
(get-model)
