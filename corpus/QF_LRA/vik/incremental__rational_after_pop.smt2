; Hand-written for VIK (this repository's license): the constant standing for a
; rational literal is defined at first use — a use after that level is popped
; must define it again.
(set-logic QF_LRA)
(declare-const a Real)
(push)
(assert (= a 0.5))
(set-info :status sat)
(check-sat)
(pop)
(assert (= a 0.5))
(assert (> a 0.6))
(set-info :status unsat)
(check-sat)
