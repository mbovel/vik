; From bitwuzla: solver/bv/udivzero.smt2 (MIT, LICENSES/bitwuzla.txt)
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-11-24)
(set-logic QF_BV)
(declare-fun s () (_ BitVec 32))
(assert (not (= (bvudiv s (_ bv0 32)) (bvnot (_ bv0 32)))))
(set-info :status unsat)
(check-sat)

