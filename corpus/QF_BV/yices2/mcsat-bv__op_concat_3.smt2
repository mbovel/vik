; From yices2: mcsat/bv/op_concat_3.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 16))
(declare-fun y () (_ BitVec 16))
(declare-fun z () (_ BitVec 32))

(assert (= x #b0000000000000000))
(assert (= y #b1111111111111111))

(assert (= z (concat x y)))

(set-info :status sat)
(check-sat)

(exit)

