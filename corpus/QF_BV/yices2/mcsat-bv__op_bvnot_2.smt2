; From yices2: mcsat/bv/op_bvnot_2.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 10))
(declare-fun y () (_ BitVec 10))

(assert (= x #b0000000000))
(assert (= y (bvnot x)))

(set-info :status sat)
(check-sat)

(exit)

