; From yices2: mcsat/bv/op_extract_5.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 64))
(declare-fun y () (_ BitVec 32))

(assert (= x #b0000000000000000000000000000000011111111111111111111111111111111))
(assert (= y ((_ extract 47 16) x)))

(set-info :status sat)
(check-sat)

(exit)

