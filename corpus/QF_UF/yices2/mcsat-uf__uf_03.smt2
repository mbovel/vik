; From yices2: mcsat/uf/uf_03.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UF)

(declare-sort s 0)
(declare-fun f (s) s)

(declare-fun x0 () s)
(declare-fun x1 () s)
(declare-fun x2 () s)

(assert (= x0 x1))
(assert (= x1 x2))

(assert (not (= (f x0) (f x2))))

(set-info :status unsat)
(check-sat)
(exit)
