; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(declare-const x Real)
(assert (is_int x))
(assert (< 0 x))
(assert (< x 1))
(set-info :status unsat)
(check-sat)
