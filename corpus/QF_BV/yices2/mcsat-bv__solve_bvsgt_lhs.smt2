; From yices2: mcsat/bv/solve_bvsgt_lhs.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun s () (_ BitVec 8))
(declare-fun x () (_ BitVec 8))

(assert (= s #b11101011))
(assert (bvsgt x s))

(set-info :status sat)
(check-sat)

(exit)
