; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
; logic: QF_LRA, the smallest the script fits, for QF_LIRA (prune logic)
(set-logic QF_LRA)
(declare-const x Real)
(assert (is_int x))
(assert (< 0 x))
(assert (< x 1))
(set-info :status unsat)
(check-sat)
