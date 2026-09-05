; Hand-written for VIK (this repository's license): an uninterpreted sort whose name holds a space. The
; sort of a function is carried as a string split on spaces, so `|a b|` read as two sorts and a
; real-valued application over it was given an integer variable, answering unsat on a satisfiable script
; (issue 11, 2026-09-04).
(set-logic QF_UFLRA)
(declare-sort |a b| 0)
(declare-fun x () |a b|)
(declare-fun y () |a b|)
(declare-fun f (|a b|) Real)
(declare-fun g (|a b| |a b|) |a b|)
(assert (< 0 (f x)))
(assert (< (f x) 1))
(set-info :status sat)
(check-sat)
(assert (= (f (g x y)) 0.5))
(assert (= (g x y) x))
(set-info :status sat)
(check-sat)
(assert (not (= (f x) 0.5)))
(set-info :status unsat)
(check-sat)
