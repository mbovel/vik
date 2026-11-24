; From yices2: mcsat/uf/issue621_higher_order_non_risky_diseq.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_AUFLIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_AUFLIA)
(declare-fun f () (Array (Array Int Bool) Bool))
(declare-fun g () (Array (Array Int Bool) Bool))

(assert (distinct f g))

(set-info :status sat)
(check-sat)
(get-value ((= f g)))
(exit)
