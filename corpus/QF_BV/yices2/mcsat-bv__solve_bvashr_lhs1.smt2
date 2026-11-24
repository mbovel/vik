; From yices2: mcsat/bv/solve_bvashr_lhs1.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)

(declare-fun c () (_ BitVec 8))
(declare-fun s () (_ BitVec 8))
(declare-fun x () (_ BitVec 8))

(assert (= (bvashr x c) s))
(assert (= c #b00001111))
(assert (= s #b11111111))

(set-info :status sat)
(check-sat)

(exit)
