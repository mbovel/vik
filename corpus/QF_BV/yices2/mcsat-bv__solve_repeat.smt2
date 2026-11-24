; From yices2: mcsat/bv/solve_repeat.smt2 (BSD-3-Clause, LICENSES/yices2.txt)
(set-logic QF_BV)
(set-info :status sat)

(declare-fun s () (_ BitVec 24))
(declare-fun x () (_ BitVec 8))

(assert (= s #b000110110001101100011011))
(assert (= ((_ repeat 3) x) s))

(set-info :status sat)
(check-sat)

(exit)
