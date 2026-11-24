; From yices2: mcsat/uf/uf_04.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UF)

(declare-sort s 0)
(declare-fun f (s) s)

(declare-fun x0 () s)
(declare-fun x1 () s)
(declare-fun x2 () s)

(declare-fun y0 () s)

(declare-fun z0 () s)

(assert (or 
   (and (= x0 y0) (= y0 x1))
   (and (= x0 z0) (= z0 x1))
))

(assert (not (= (f x0) (f x2))))

(set-info :status sat)
(check-sat)
(exit)
