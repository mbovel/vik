; From yices2: mcsat/bv/op_concat_1.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 5))
(declare-fun y () (_ BitVec 5))
(declare-fun z () (_ BitVec 10))

(assert (= x #b00000))
(assert (= y #b11111))

(assert (= z (concat x y)))

(set-info :status sat)
(check-sat)

(exit)

