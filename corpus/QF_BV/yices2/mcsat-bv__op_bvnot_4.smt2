; From yices2: mcsat/bv/op_bvnot_4.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 40))
(declare-fun y () (_ BitVec 40))

(assert (= x #b1111111111111111111100000000000000000000))
(assert (= y (bvnot x)))

(set-info :status sat)
(check-sat)

(exit)

