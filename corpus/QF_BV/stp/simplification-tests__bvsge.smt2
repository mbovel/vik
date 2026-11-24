; From stp: simplification-tests/bvsge.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --exit-after-CNF %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :smt-lib-version 2.0)
(set-info :category "check")
(set-info :status sat)
(declare-fun v0 () (_ BitVec 2))
(assert (bvsge v0 v0))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(exit)
