; From yices2: mcsat/bv/op_extract_3.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 5))
(declare-fun y () (_ BitVec 4))

(assert (= x #b01010))
(assert (= y ((_ extract 3 0) x)))

(set-info :status sat)
(check-sat)

(exit)

