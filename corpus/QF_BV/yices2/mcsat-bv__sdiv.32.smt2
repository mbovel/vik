; From yices2: mcsat/bv/sdiv.32.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(declare-fun x0 () (_ BitVec 32))
(assert 
  (bvule (_ bv130972 32) (bvsdiv (bvadd (bvsub x0 (_ bv1 32)) (_ bv60000000 32)) x0))
)
(set-info :status sat)
(check-sat)
(exit)
