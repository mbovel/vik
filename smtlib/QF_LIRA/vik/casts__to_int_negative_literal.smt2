; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(assert (= (to_int (- 5.5)) (- 6)))
(assert (= (to_int 2.5) 2))
(set-info :status sat)
(check-sat)
