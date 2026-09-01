; From princess: rationals/casts.smt2 (BSD, LICENSES/princess.txt)

(declare-const x Real)
(declare-const y Real)

; (assert (not (= x (to_int (to_real x)))))

(assert (not (= (to_int (+ x y)) (+ (to_int x) (to_int y)))))

(set-info :status sat)
(check-sat)
(get-model)
