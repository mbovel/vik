; From yices2: wd/issue308.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(declare-const bv_36-1 (_ BitVec 36))
(assert (bvugt (bvsrem (_ bv0 67) (bvsdiv (concat (_ bv0 31) bv_36-1) (concat (_ bv0 31) bv_36-1))) (_ bv0 67)))
(set-info :status unsat)
(check-sat)

