; From stp: sample-smt-tests/bvugt.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
; CHECK-NEXT: ^unsat

(set-logic QF_BV)
(declare-const bv_40-0 (_ BitVec 40))
(assert (bvugt (bvurem ((_ rotate_right 6) bv_40-0) bv_40-0) bv_40-0))
(set-info :status unsat)
(check-sat)