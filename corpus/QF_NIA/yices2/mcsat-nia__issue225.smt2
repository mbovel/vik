; From yices2: mcsat/nia/issue225.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-info :smt-lib-version 2.6)
(set-logic QF_NIA)

(declare-fun x () Int)

(push 1)
(assert (= 0 (div x 5)))
(pop 1)

(assert (and (< x 0) (= 0 (div x 5))))

(set-info :status unsat)
(check-sat)
