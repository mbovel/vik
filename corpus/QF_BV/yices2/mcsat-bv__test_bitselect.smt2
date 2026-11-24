; From yices2: mcsat/bv/test_bitselect.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun x () (_ BitVec 5))

(assert (= #b1 ((_ extract 3 3) x)))

(set-info :status sat)
(check-sat)

(exit)

