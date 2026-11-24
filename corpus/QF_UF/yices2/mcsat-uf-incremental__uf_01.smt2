; From yices2: mcsat/uf/incremental/uf_01.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
; logic: QF_UF, the smallest the script fits, for QF_UFNRA (prune logic)
(set-logic QF_UF)
(declare-sort s 0)
(declare-fun x () s)
(declare-fun y () s)
(declare-fun z () s)

(set-info :status sat)
(check-sat)

(push 1)
(assert (= x y))
(set-info :status sat)
(check-sat)

(push 1)
(assert (= y z))
(set-info :status sat)
(check-sat)

(push 1)
(assert (not (= x z)))
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
