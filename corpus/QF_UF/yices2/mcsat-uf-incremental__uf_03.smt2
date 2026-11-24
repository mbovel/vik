; From yices2: mcsat/uf/incremental/uf_03.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_UF, the smallest the script fits, for QF_UFNRA (prune logic)
(set-logic QF_UF)

(declare-sort s 0)
(declare-fun f (s) s)

(declare-fun x0 () s)
(declare-fun x1 () s)
(declare-fun x2 () s)

(set-info :status sat)
(check-sat)

(push 1)
(assert (= x0 x1))
(set-info :status sat)
(check-sat)

(push 1)
(assert (= x1 x2))
(set-info :status sat)
(check-sat)

(push 1)
(assert (not (= (f x0) (f x2))))
(set-info :status unsat)
(check-sat)

(pop 1)
(set-info :status sat)
(check-sat)

(pop 1)
(set-info :status sat)
(check-sat)

(pop 1)
(set-info :status sat)
(check-sat)

(exit)
