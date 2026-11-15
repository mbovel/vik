; From princess: rationals/casts3.smt2 (BSD, LICENSES/princess.txt)
; EXPECT: sat
; EXPECT: ((define-fun x () Real (/ 1.0 2.0)))
; logic: QF_LRA, the smallest the script fits, which set none (prune logic)
(set-logic QF_LRA)

(declare-const x Real)

(assert (not (is_int x)))

(set-info :status sat)
(check-sat)
(get-model)
