; From bitwuzla: solver/bv/smtflet.smt2 (MIT, LICENSES/bitwuzla.txt)
(set-info :status unsat)
(set-logic QF_BV)
(declare-fun a () Bool)
(assert (and a (not a)))
(check-sat)

