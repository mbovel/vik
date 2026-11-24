; From stp: simplification-tests/bvsgt.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --exit-after-CNF %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :smt-lib-version 2.0)
(set-info :category "check")
(set-info :status unsat)
(declare-fun p () Bool)
(assert (bvsgt (_ bv4 3) (ite p (_ bv7 3)(_ bv0 3) ) ) )
; CHECK-NEXT: ^unsat
(set-info :status unsat)
(check-sat)
(exit)
