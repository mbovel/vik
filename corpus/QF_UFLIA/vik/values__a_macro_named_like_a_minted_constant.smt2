; Hand-written for VIK (this repository's license): a define-fun named `ite!1` and a term-level `ite`
; the lowering names `ite!N`: the value of the macro is the macro's, not the fresh constant's (issue 46).
; EXPECT: sat
; EXPECT: ((ite!1 5) ((+ ite!1 1) 6) ((rat!1 x) 2) (x 1))
(set-logic QF_UFLIA)
(set-option :produce-models true)
(define-fun ite!1 () Int 5)
(define-fun rat!1 ((y Int)) Int (+ y 1))
(declare-const x Int)
(declare-const b Bool)
(assert b)
(assert (= x (ite b 1 2)))
(set-info :status sat)
(check-sat)
(get-value (ite!1 (+ ite!1 1) (rat!1 x) x))
