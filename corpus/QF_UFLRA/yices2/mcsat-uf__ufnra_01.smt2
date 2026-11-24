; From yices2: mcsat/uf/ufnra_01.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_UFLRA, the smallest the script fits, for QF_UFNRA (prune logic)
(set-logic QF_UFLRA)

(set-info :smt-lib-version 2.0)

(declare-fun x () Real)
(declare-fun f (Real) Real)

(assert (or (= x 1) (= x 2)))

(assert (not (= (f 1) (f x))))
(assert (not (= (f 2) (f x))))

(set-info :status unsat)
(check-sat)
(exit)
