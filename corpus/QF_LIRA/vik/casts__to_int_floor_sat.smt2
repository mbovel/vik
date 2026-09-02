; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(declare-const x Real)
(assert (<= 2 x))
(assert (< x 3))
(assert (= (to_int x) 2))
(set-info :status sat)
(check-sat)
