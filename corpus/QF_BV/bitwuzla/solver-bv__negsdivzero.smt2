; From bitwuzla: solver/bv/negsdivzero.smt2 (MIT, LICENSES/bitwuzla.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-11-24)
(set-logic QF_BV)
(declare-fun s () (_ BitVec 4))
(assert (bvslt s (_ bv0 4)))
(assert (not (= (bvsdiv s (_ bv0 4)) (_ bv1 4))))
(set-info :status unsat)
(check-sat)

