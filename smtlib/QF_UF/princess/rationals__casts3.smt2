; From princess: rationals/casts3.smt2 (BSD, LICENSES/princess.txt)

(declare-const x Real)

(assert (not (is_int x)))

(set-info :status sat)
(check-sat)
(get-model)
