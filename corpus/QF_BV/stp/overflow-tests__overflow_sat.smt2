; From stp: overflow-tests/overflow_sat.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
(set-logic QF_BV)
(declare-fun x () (_ BitVec 8))
(declare-fun y () (_ BitVec 8))
(assert (bvsmulo x y))
(assert (bvuaddo x y))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(exit)
