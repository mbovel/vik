; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(assert (is_int 2.0))
(assert (not (is_int 2.5)))
(set-info :status sat)
(check-sat)
