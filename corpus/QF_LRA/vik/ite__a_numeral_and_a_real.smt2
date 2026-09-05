; Hand-written for VIK (this repository's license): an `ite` whose branches are a numeral and a real term.
; It took the sort of its first branch, `Int`, so its constant was integral and its comparisons used the
; integer bounds, and a satisfiable script answered unsat (issue 15, 2026-09-05).
(set-logic QF_LRA)
(declare-const x Real)
(declare-const y Real)
(declare-const b Bool)
(assert (not b))
(assert (< 0 (ite b 1 x)))
(assert (< (ite b 1 x) 1))
(set-info :status sat)
(check-sat)
(assert (= y (ite b x 1)))
(assert (< y 1.5))
(set-info :status sat)
(check-sat)
(assert (< y 0.5))
(set-info :status unsat)
(check-sat)
