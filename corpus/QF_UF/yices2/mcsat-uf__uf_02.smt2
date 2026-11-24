; From yices2: mcsat/uf/uf_02.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UF)

(declare-sort s 0)

(declare-fun x0 () s)
(declare-fun x1 () s)

(declare-fun y0 () s)

(declare-fun z0 () s)

(assert (or 
   (and (= x0 y0) (= y0 x1))
   (and (= x0 z0) (= z0 x1))
))

(assert (not (= x0 x1)))

(set-info :status unsat)
(check-sat)
(exit)
