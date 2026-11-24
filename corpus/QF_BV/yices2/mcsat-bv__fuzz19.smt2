; From yices2: mcsat/bv/fuzz19.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)

(declare-fun x () (_ BitVec 2))
(declare-fun y () (_ BitVec 2))
(declare-fun z () (_ BitVec 1))

(assert (= (bvand z ((_ extract 1 1) (bvmul x y))) (_ bv1 1)))

(set-info :status sat)
(check-sat)
(exit)
