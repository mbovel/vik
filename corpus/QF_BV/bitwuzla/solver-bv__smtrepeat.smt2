; From bitwuzla: solver/bv/smtrepeat.smt2 (MIT, LICENSES/bitwuzla.txt)
(set-logic QF_BV)
(set-info :status sat)
(assert (= ((_ repeat 3) (_ bv0 1)) (_ bv0 3)))
(assert (= ((_ repeat 4) (_ bv10 4)) (_ bv43690 16)))
(check-sat)

