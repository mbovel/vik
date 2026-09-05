; Hand-written for VIK (this repository's license): `define-const`, a `define-fun` of no parameter, which
; the parser dropped, so that the symbol was unknown at its use (issue 30, 2026-09-05).
(set-logic QF_UF)
(declare-const x Bool)
(declare-const y Bool)
(define-const p Bool (and x y))
(assert p)
(set-info :status sat)
(check-sat)
(assert (not x))
(set-info :status unsat)
(check-sat)
