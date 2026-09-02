; Hand-written for VIK (this repository's license): the impossible value first —
; an unsat solve must not poison the satisfiable one after the pop.
(declare-const g (-> Int Int))
(assert (or (= g (lambda ((x Int)) (+ x 1))) (= g (lambda ((x Int)) (+ x 5)))))
(push)
(assert (= (g 0) 3))
(set-info :status unsat)
(check-sat)
(pop)
(push)
(assert (= (g 0) 5))
(set-info :status sat)
(check-sat)
(pop)
