; Hand-written for VIK (this repository's license): `get-value` of real terms. A real sum lost its
; fraction, a rational literal printed as the lowering's own constant, and a Boolean item was evaluated
; wrongly, all on the path that values the lowered term under the model (issue 28, 2026-09-05).
; EXPECT: sat
; EXPECT: (((+ x 0.5) (/ 9.0 2.0)) (1.5 (/ 3.0 2.0)) ((/ x 2) 2.0) (y (/ (- 3.0) 4.0)) ((* 2 y) (/ (- 3.0) 2.0)) ((< y x) true) ((= (ite (< x y) x y) y) true))
(set-logic QF_LRA)
(set-option :produce-models true)
(declare-const x Real)
(declare-const y Real)
(assert (= x 4.0))
(assert (= y (- 0.75)))
(set-info :status sat)
(check-sat)
(get-value ((+ x 0.5) 1.5 (/ x 2) y (* 2 y) (< y x) (= (ite (< x y) x y) y)))
