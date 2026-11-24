; From yices2: mcsat/uf/incremental/uf_02.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_UF, the smallest the script fits, for QF_UFNRA (prune logic)
(set-logic QF_UF)

(declare-sort s 0)

(declare-fun x0 () s)
(declare-fun x1 () s)

(declare-fun y0 () s)

(declare-fun z0 () s)

(set-info :status sat)
(check-sat)

(push 1)
(assert (or 
   (and (= x0 y0) (= y0 x1))
   (and (= x0 z0) (= z0 x1))
))
(set-info :status sat)
(check-sat)

(push 1)
(assert (not (= x0 x1)))
(set-info :status unsat)
(check-sat)

(pop 1)
(set-info :status sat)
(check-sat)

(pop 1)
(set-info :status sat)
(check-sat)

(exit)
