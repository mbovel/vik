; From yices2: mcsat/uf/issue621_non_sensitive_read_over_update.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_AUFLIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_AUFLIA)
(declare-fun a () (Array Int Int))
(declare-fun i () Int)

(assert (not (= (select (store a i 10) i) 10)))

(set-info :status unsat)
(check-sat)
(exit)
