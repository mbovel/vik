; From yices2: mcsat/bv/solve_rotate_right.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun s () (_ BitVec 8))
(declare-fun x () (_ BitVec 8))

(assert (= s #b00111001))
(assert (= ((_ rotate_right 3) x) s))

(set-info :status sat)
(check-sat)

(exit)
