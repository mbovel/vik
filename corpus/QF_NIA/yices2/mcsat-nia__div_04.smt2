; From yices2: mcsat/nia/div_04.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_NIA)
(set-info :smt-lib-version 2.0)
(declare-fun x () Int)
(declare-fun y () Int)

(assert (not (= (div x 0) (div y 0))))

(set-info :status sat)
(check-sat)
(exit)
