; From yices2: mcsat/uf/incremental/uf_05.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UF)

(declare-fun x1 () Bool)
(declare-fun y1 () Bool)

(declare-fun x2 () Bool)
(declare-fun y2 () Bool)

(push 1)
(assert (or (= x1 y1) (= x2 y2)))
(set-info :status sat)
(check-sat)

(declare-fun f (Bool) Bool)

(push 1) 
(assert (distinct (f x1) (f y1)))
(set-info :status sat)
(check-sat)

(push 1)
(assert (distinct (f x2) (f y2)))
(set-info :status unsat)
(check-sat)

(pop 1)
(set-info :status sat)
(check-sat)

(pop 1)
(set-info :status sat)
(check-sat)

(exit)
