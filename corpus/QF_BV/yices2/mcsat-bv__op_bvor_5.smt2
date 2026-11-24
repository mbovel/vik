; From yices2: mcsat/bv/op_bvor_5.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 86))
(declare-fun y () (_ BitVec 86))
(declare-fun z () (_ BitVec 86))

(declare-fun o1 () (_ BitVec 86))
(declare-fun o2 () (_ BitVec 86))
(declare-fun o3 () (_ BitVec 86))

(assert (= x #b00000000000000000000000000000000000000000000000000000000000000000000000000000000000000))
(assert (= y #b11111111111111111111111111111111111111111111111111111111111111111111111111111111111111))
(assert (= z #b01010010100101001010101010010100101001010101010101010101010101010101010101010101010101))

(assert (= o1 (bvor x y)))
(assert (= o2 (bvor y z)))
(assert (= o3 (bvor z x)))

(set-info :status sat)
(check-sat)

(exit)

