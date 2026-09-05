; Hand-written for VIK (this repository's license): two shared real terms less than one apart. The clause
; that decides a pair — equal, below, or above — split at `c - 1`, which covers every integer and leaves
; the open band `(c - 1, c)` uncovered over the reals, so the pair was forced equal and a satisfiable
; script answered unsat (issue 8, 2026-09-04).
(set-logic QF_UFLRA)
(declare-fun f (Real) Real)
(declare-const u Real)
(declare-const v Real)
(assert (= u 0.0))
(assert (= v 0.5))
(assert (= (f u) 0.5))
(assert (= (f v) 0.0))
(set-info :status sat)
(check-sat)
(assert (= (f 0.25) 0.75))
(set-info :status sat)
(check-sat)
(assert (= u v))
(set-info :status unsat)
(check-sat)
