; From bitwuzla: solver/bv/nego1.smt2 (MIT, LICENSES/bitwuzla.txt)
(set-logic QF_BV)
(set-info :status sat)
(declare-const v (_ BitVec 3))
(assert (not (bvnego v)))
(check-sat)

