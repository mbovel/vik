; Hand-written for VIK (this repository's license): the first `store` of the script comes after a
; `check-sat`. Whether the problem has stores at all was decided at the first solve and remembered, so a
; store declared later got no read-over-write axiom and a contradiction answered sat (issue 13,
; 2026-09-04).
(set-logic QF_ALIA)
(set-option :global-declarations true)
(declare-const x Int)
(assert (> x 0))
(set-info :status sat)
(check-sat)
(declare-const a (Array Int Int))
(declare-const i Int)
(declare-const j Int)
(assert (not (= (select (store a i 1) i) 1)))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (= (select (store a i 1) j) 2))
(set-info :status sat)
(check-sat)
(assert (= i j))
(set-info :status unsat)
(check-sat)
