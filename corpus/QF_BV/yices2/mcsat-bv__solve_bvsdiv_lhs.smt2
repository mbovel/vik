; From yices2: mcsat/bv/solve_bvsdiv_lhs.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun c () (_ BitVec 8))
(declare-fun s () (_ BitVec 8))
(declare-fun x () (_ BitVec 8))

(assert (= c #b00000010))
(assert (= s #b11110110))
(assert (= (bvsdiv x c) s))

(set-info :status sat)
(check-sat)

(exit)
