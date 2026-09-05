; Hand-written for VIK (this repository's license): a `define-fun` body that reads a global, called from
; another macro whose parameter has the global's name, and under a `let` that shadows it. The body was
; expanded in the caller's environment, so the parameter captured the global and a satisfiable script
; answered unsat (issue 10, 2026-09-04).
(set-logic QF_LIA)
(set-option :global-declarations true)
(declare-fun a () Int)
(define-fun g ((u Int)) Int (+ u a))
(define-fun f ((a Int)) Int (g 0))
(assert (= (f 5) 0))
(set-info :status sat)
(check-sat)
(assert (= a 1))
(set-info :status unsat)
(check-sat)
(reset-assertions)
(declare-fun x () Int)
(define-fun h ((y Int)) Int (+ y x))
(assert (= (let ((x 100)) (h 1)) 1))
(set-info :status sat)
(check-sat)
(assert (= x 100))
(set-info :status unsat)
(check-sat)
