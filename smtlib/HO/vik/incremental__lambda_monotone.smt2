; Hand-written for VIK (this repository's license): no pops — the second value
; contradicts the first on the same level stack.
(declare-const g (-> Int Int))
(assert (or (= g (lambda ((x Int)) (+ x 1))) (= g (lambda ((x Int)) (+ x 5)))))
(push)
(assert (= (g 0) 5))
(set-info :status sat)
(check-sat)
(assert (= (g 0) 3))
(set-info :status unsat)
(check-sat)
(pop)
