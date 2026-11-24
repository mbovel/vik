; From yices2: mcsat/bv/op_bvnot_3.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 20))
(declare-fun y () (_ BitVec 20))

(assert (= x #b11111111111111111111))
(assert (= y (bvnot x)))

(set-info :status sat)
(check-sat)

(exit)

