; Hand-written for VIK (this repository's license): `(exit)` ends the script, and what follows is not
; read (issue 52) — VIK played it, and answered a check-sat past the exit.
(set-logic QF_UF)
(declare-const p Bool)
(assert p)
(set-info :status sat)
(check-sat)
(exit)
(assert (not p))
(set-info :status unsat)
(check-sat)
(this is not even a command)
