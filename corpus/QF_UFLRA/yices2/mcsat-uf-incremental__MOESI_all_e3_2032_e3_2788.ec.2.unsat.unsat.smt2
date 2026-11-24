; From yices2: mcsat/uf/incremental/MOESI_all_e3_2032_e3_2788.ec.2.unsat.unsat.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_UFLRA)

(declare-fun f (Real) Real)
(declare-fun p0 (Real) Bool)
(declare-fun p1 (Real) Bool)

(assert (= (ite (p1 0) (- 1) 0) (f 0)))
(assert (= (= 1 (f 0)) true))
(assert (= (and (= (f 0) 0) (not (p1 0))) (p0 0)))
(assert (= true (p0 (- 1))))

(push 1)

(assert (not (p0 0)))
(set-info :status unsat)
(check-sat)

(pop 1)

(assert (not (p0 1)))

(set-info :status unsat)
(check-sat)

(exit)
