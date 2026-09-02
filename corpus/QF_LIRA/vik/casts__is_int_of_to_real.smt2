; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(declare-const n Int)
(assert (not (is_int (to_real n))))
(set-info :status unsat)
(check-sat)
