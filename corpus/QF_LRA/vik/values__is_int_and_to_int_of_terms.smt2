; Hand-written for VIK (this repository's license): the value of `is_int` and `to_int` of a term that is
; not a literal — a real constant, a sum — computed from the model (issue 45). VIK read them through the
; lowered term, whose fresh `floor!N` constant the model does not value: `is_int` came out false and
; `to_int` as the constant's name.
; EXPECT: sat
; EXPECT: (((is_int r) true) ((is_int s) false) ((is_int (+ r 1.0)) true) ((ite (is_int r) 1 0) 1) ((to_int (+ r 0.5)) 3) ((to_int (- s)) (- 3)) ((to_int s) 2))
; EXPECT: (((let ((k (to_int s))) k) 2) ((is_int (* 2.0 s)) true) ((=> (is_int s) (is_int r)) true) ((xor (is_int s) (is_int r)) true))
; logic: QF_LRA, the smallest the script fits, for QF_LIRA (prune logic)
(set-logic QF_LRA)
(set-option :produce-models true)
(declare-const r Real)
(declare-const s Real)
(assert (= r 3.0))
(assert (= s 2.5))
(set-info :status sat)
(check-sat)
(get-value ((is_int r) (is_int s) (is_int (+ r 1.0)) (ite (is_int r) 1 0) (to_int (+ r 0.5)) (to_int (- s)) (to_int s)))
(get-value ((let ((k (to_int s))) k) (is_int (* 2.0 s)) (=> (is_int s) (is_int r)) (xor (is_int s) (is_int r))))
