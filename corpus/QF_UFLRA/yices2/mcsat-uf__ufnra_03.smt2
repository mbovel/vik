; From yices2: mcsat/uf/ufnra_03.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_UFLRA, the smallest the script fits, for QF_UFNRA (prune logic)
(set-logic QF_UFLRA)

(set-info :smt-lib-version 2.0)

(declare-fun x () Real)
(declare-fun y () Real)
(declare-fun z () Real)
(declare-fun f (Real) Real)

(assert (<= (f (f (* 5 x))) (f (f (+ y z)))))

(set-info :status sat)
(check-sat)
(exit)
