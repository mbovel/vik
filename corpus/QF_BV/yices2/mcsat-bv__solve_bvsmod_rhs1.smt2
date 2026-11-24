; From yices2: mcsat/bv/solve_bvsmod_rhs1.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)

(declare-fun y () (_ BitVec 2))

(assert (= #b00 (bvsmod #b00 y)))
(assert (= ((_ extract 1 1) y) #b1))

(set-info :status sat)
(check-sat)
