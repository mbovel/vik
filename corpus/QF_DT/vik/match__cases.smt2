; Hand-written for VIK (this repository's license): `match` in a term, in a
; formula and in a `define-fun` body, with constructor patterns, nullary
; constructors and a variable pattern catching the rest — read as testers
; and selectors under an `ite`.
; EXPECT: sat
; EXPECT: ((n 5) ((area s) 7))
; EXPECT: unsat
; EXPECT: sat
(set-logic ALL)
(set-option :produce-models true)
(declare-datatype List ((nil) (cons (head Int) (tail List))))
(declare-datatype Shape ((circle (radius Int)) (rect (w Int) (h Int)) (point)))
(declare-const l List)
(declare-const s Shape)
(declare-const n Int)
(define-fun area ((x Shape)) Int (match x ((point 0) ((circle r) (* 3 r)) ((rect a b) (+ a b)))))
(assert (= n (match l (((cons h t) h) (nil 0)))))
(assert (match s ((point false) (other (> (area other) 6)))))
(assert (= (area s) 7))
(assert (= l (cons 5 nil)))
(set-info :status sat)
(check-sat)
(get-value (n (area s)))
(push 1)
(assert (match s (((circle r) (= r 2)) ((rect a b) (= a b)) (point true))))
(set-info :status unsat)
(check-sat)
(pop 1)
(push 1)
(assert (match l ((nil false) ((cons h t) (= t nil)))))
(set-info :status sat)
(check-sat)
(pop 1)
