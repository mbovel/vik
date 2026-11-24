; From yices2: mcsat/lra/incremental/MOESI_all_e3_2032_e3_2788.ec.2.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_LRA)

(declare-fun a () Bool)
(declare-fun b () Bool)
(declare-fun c () Bool)
(declare-fun d () Bool)
(declare-fun x () Real)

(assert (= x (ite a (- 1) 0)))
(assert (= (= 1 x) a))
(assert (= (and (= x 0) (not a)) c))
(assert d)

(push 1)

(assert (not c))

(set-info :status unsat)
(check-sat)

(pop 1)

(assert (not b))

(set-info :status sat)
(check-sat)

(exit)
