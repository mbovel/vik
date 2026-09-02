; From princess: smtlib-parser/define-fun-bug.smt2 (BSD, LICENSES/princess.txt)
(set-logic AUFLIA)
(define-fun p1 () Bool true)
(define-fun p2 ((x Int)) Bool (> x 0))

(assert (not (=> (p2 5) p1)))

(set-info :status unsat)
(check-sat)
(exit)