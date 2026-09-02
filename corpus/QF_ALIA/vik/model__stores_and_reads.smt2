; Hand-written for VIK (this repository's license): the model of a script over
; integer arrays values the arrays — a default and the indices the script read
; or wrote, as `store`s over a constant array — and `get-value` reads through
; them, `store`s in the query included.
; EXPECT: sat
; EXPECT: (((select b 1) 10) ((select b 2) 20) ((select a 2) 20) ((select (store b i 5) i) 5) ((= a b) false))
; EXPECT: ((define-fun a () (Array Int Int) (store (store ((as const (Array Int Int)) 0) 2 20) 3 31)) (define-fun b () (Array Int Int) (store (store (store ((as const (Array Int Int)) 0) 1 10) 2 20) 3 31)) (define-fun i () Int 3))
(set-logic QF_ALIA)
(set-option :produce-models true)
(declare-const a (Array Int Int))
(declare-const b (Array Int Int))
(declare-const i Int)
(assert (= b (store a 1 10)))
(assert (= (select a 2) 20))
(assert (> (select b i) 30))
(assert (distinct i 1 2))
(set-info :status sat)
(check-sat)
(get-value ((select b 1) (select b 2) (select a 2) (select (store b i 5) i) (= a b)))
(get-model)
