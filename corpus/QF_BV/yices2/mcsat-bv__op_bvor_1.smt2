; From yices2: mcsat/bv/op_bvor_1.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 5))
(declare-fun y () (_ BitVec 5))
(declare-fun z () (_ BitVec 5))

(declare-fun o1 () (_ BitVec 5))
(declare-fun o2 () (_ BitVec 5))
(declare-fun o3 () (_ BitVec 5))


(assert (= x #b00000))
(assert (= y #b11111))
(assert (= z #b01010))

(assert (= o1 (bvor x y)))
(assert (= o2 (bvor y z)))
(assert (= o3 (bvor z x)))

(set-info :status sat)
(check-sat)

(exit)

