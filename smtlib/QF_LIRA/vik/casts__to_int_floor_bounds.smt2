; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(declare-const x Real)
(assert (= (to_int x) 2))
(assert (< x 2))
(set-info :status unsat)
(check-sat)
