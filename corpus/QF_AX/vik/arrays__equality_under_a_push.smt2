; Hand-written for VIK (this repository's license): an array equality asserted under a `push`, then its
; negation after the `pop`. An asserted equality needs no extensionality witness, and one asserted under a
; level was recorded like a permanent one, so its negation got none and answered sat (issue 19,
; 2026-09-05).
(set-logic QF_AX)
(declare-sort I 0)
(declare-sort E 0)
(declare-const a (Array I E))
(declare-const i I)
(push 1)
(assert (= a (store a i (select a i))))
(set-info :status sat)
(check-sat)
(pop 1)
(assert (not (= a (store a i (select a i)))))
(set-info :status unsat)
(check-sat)
