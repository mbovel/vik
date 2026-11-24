; From yices2: mcsat/uf/issue621_function_id_already_equal.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_AX, the smallest the script fits, for ALL (prune logic)
(set-logic QF_AX)
(declare-fun f () (Array Bool Bool))
(declare-fun g () (Array Bool Bool))

(assert (= f g))
(assert (= (select f false) false))
(assert (= (select g false) false))

(set-info :status sat)
(check-sat)
(exit)
