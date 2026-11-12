; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
; logic: QF_LRA, the smallest the script fits, for QF_LIRA (prune logic)
(set-logic QF_LRA)
(assert (is_int 2.0))
(assert (not (is_int 2.5)))
(set-info :status sat)
(check-sat)
