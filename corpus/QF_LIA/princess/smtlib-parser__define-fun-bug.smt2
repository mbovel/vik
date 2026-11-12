; From princess: smtlib-parser/define-fun-bug.smt2 (BSD, LICENSES/princess.txt)
; logic: QF_LIA, the smallest the script fits, for AUFLIA (prune logic)
(set-logic QF_LIA)
(define-fun p1 () Bool true)
(define-fun p2 ((x Int)) Bool (> x 0))

(assert (not (=> (p2 5) p1)))

(set-info :status unsat)
(check-sat)
(exit)
