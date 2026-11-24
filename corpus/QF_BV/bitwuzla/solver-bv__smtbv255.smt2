; From bitwuzla: solver/bv/smtbv255.smt2 (MIT, LICENSES/bitwuzla.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-11-24)
(set-info :status unsat)
(set-logic QF_BV)
(assert (not (= (_ bv255 8) (bvnot (_ bv0 8)))))
(check-sat)

