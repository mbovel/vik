; From yices2: mcsat/bv/solve_bvsmod_rhs.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun c () (_ BitVec 8))
(declare-fun s () (_ BitVec 8))
(declare-fun x () (_ BitVec 8))

(assert (= c #b11100011))
(assert (= s #b00000010))
(assert (= (bvsmod c x) s))

(set-info :status sat)
(check-sat)

(exit)
