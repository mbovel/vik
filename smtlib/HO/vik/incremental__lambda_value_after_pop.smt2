; Hand-written for VIK (this repository's license): a value for `(g 0)` asserted
; under a push must not linger after the pop — the contradicting value is unsat.
(declare-const g (-> Int Int))
(assert (or (= g (lambda ((x Int)) (+ x 1))) (= g (lambda ((x Int)) (+ x 5)))))
(push)
(assert (= (g 0) 5))
(set-info :status sat)
(check-sat)
(pop)
(push)
(assert (= (g 0) 3))
(set-info :status unsat)
(check-sat)
(pop)
