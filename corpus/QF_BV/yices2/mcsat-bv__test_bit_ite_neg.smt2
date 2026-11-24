; From yices2: mcsat/bv/test_bit_ite_neg.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 1))
(declare-fun b () Bool)

(assert (= x (ite b #b0 #b1)))

(set-info :status sat)
(check-sat)

(exit)

