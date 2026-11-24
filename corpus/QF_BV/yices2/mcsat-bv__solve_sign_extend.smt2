; From yices2: mcsat/bv/solve_sign_extend.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun s () (_ BitVec 16))
(declare-fun x () (_ BitVec 8))

(assert (= s #b1111111110111001))
(assert (= ((_ sign_extend 8) x) s))

(set-info :status sat)
(check-sat)

(exit)
