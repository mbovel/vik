; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(declare-const n Int)
(assert (< 2 (to_real n)))
(assert (< (to_real n) 3))
(set-info :status unsat)
(check-sat)
