; From yices2: mcsat/bv/solve_bvadd_lhs.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun c () (_ BitVec 8))
(declare-fun s () (_ BitVec 8))
(declare-fun x () (_ BitVec 8))

(assert (= c #b01010111))
(assert (= s #b01110111))
(assert (= (bvadd x c) s))

(set-info :status sat)
(check-sat)

(exit)
