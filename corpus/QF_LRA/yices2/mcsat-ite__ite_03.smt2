; From yices2: mcsat/ite/ite_03.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_LRA, the smallest the script fits, for QF_NRA (prune logic)
(set-logic QF_LRA)
(set-info :smt-lib-version 2.0)
(declare-fun b () Bool)
(declare-fun x () Bool)
(declare-fun y () Bool)

(assert (ite b x y))
(assert (ite (not b) y x))

(assert (not x))
(assert (not y))

(set-info :status unsat)
(check-sat)
(exit)
