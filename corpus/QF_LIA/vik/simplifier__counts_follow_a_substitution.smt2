; Hand-written for VIK (this repository's license): the integer shape of the stale-count elimination
; (issue 12, 2026-09-04): `x := y` puts a second `y` into the conjunction, matching the total taken
; before, and `y = 3` in another assertion was lost.
(set-logic QF_LIA)
(declare-const x Int)
(declare-const y Int)
(declare-const z Int)
(assert (and (= x y) (= x z) (= x 4)))
(assert (= y 3))
(set-info :status unsat)
(check-sat)
