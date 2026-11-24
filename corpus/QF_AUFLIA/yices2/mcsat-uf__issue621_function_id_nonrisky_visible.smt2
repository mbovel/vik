; From yices2: mcsat/uf/issue621_function_id_nonrisky_visible.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_AUFLIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_AUFLIA)
(declare-fun f () (Array Int Bool))
(declare-fun g () (Array Int Bool))

(assert (= f g))
(assert (= (select f 0) true))
(assert (= (select g 0) true))

(set-info :status sat)
(check-sat)
(exit)
