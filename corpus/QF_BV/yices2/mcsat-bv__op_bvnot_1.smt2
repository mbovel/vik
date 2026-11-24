; From yices2: mcsat/bv/op_bvnot_1.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 5))
(declare-fun y () (_ BitVec 5))

(assert (= x #b00000))
(assert (= y (bvnot x)))

(set-info :status sat)
(check-sat)

(exit)

