; Hand-written for VIK (this repository's license): the casts of Reals_Ints.
(set-logic QF_LIRA)
(declare-const n Int)
(declare-const x Real)
(assert (= x (+ (to_real n) 0.5)))
(assert (is_int x))
(set-info :status unsat)
(check-sat)
