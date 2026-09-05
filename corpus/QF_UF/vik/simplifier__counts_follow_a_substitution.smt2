; Hand-written for VIK (this repository's license): a conjunction of equalities on one constant, and a
; disequality between two of the others in a separate assertion. The simplifier eliminated the constants
; against occurrence counts taken once, before any substitution, so after `c := d` the count of `d` in
; the conjunction matched its stale total, `d` passed as local and the conjunction was simplified to
; true: a contradiction answered sat (issue 12, 2026-09-04).
(set-logic QF_UF)
(set-option :global-declarations true)
(declare-sort U 0)
(declare-const c U)
(declare-const d U)
(declare-const e U)
(declare-const g U)
(assert (and (= c d) (= c e) (= c g)))
(assert (not (= d e)))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (and (= c d) (= c e) (= c g)))
(assert (not (= d g)))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (and (= c d) (= c e)))
(assert (= d e))
(set-info :status sat)
(check-sat)
