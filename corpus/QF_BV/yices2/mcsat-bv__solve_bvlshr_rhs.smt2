; From yices2: mcsat/bv/solve_bvlshr_rhs.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun c () (_ BitVec 8))
(declare-fun s () (_ BitVec 8))
(declare-fun x () (_ BitVec 8))

(assert (= c #b10101011))
(assert (= s #b00010101))
(assert (= (bvlshr c x) s))

(set-info :status sat)
(check-sat)

(exit)
