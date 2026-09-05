; From princess: rationals/casts3.smt2 (BSD, LICENSES/princess.txt)
; EXPECT: sat
; EXPECT: ((define-fun x () Real (/ (- 1.0) 2.0)))

(declare-const x Real)

(assert (not (is_int x)))

(set-info :status sat)
(check-sat)
(get-model)
