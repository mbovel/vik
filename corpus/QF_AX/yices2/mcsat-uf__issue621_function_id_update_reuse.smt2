; From yices2: mcsat/uf/issue621_function_id_update_reuse.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_AX, the smallest the script fits, for ALL (prune logic)
(set-logic QF_AX)
(declare-fun a () (Array Bool Bool))
(declare-fun b () (Array Bool Bool))

(assert (= b (store a false true)))
(assert (= (select a false) false))
(assert (= (select b false) true))

(set-info :status sat)
(check-sat)
(exit)
