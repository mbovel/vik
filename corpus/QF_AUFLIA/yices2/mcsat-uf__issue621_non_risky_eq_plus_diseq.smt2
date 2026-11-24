; From yices2: mcsat/uf/issue621_non_risky_eq_plus_diseq.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_AUFLIA, the smallest the script fits, for ALL (prune logic)
(set-logic QF_AUFLIA)
(declare-fun f () (Array Int Bool))
(declare-fun g () (Array Int Bool))

(assert (= f g))
(assert (distinct f g))

(set-info :status unsat)
(check-sat)
(exit)
