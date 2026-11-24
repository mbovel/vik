; From stp: simplification-tests/harald.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --exit-after-CNF %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :smt-lib-version 2.0)
(set-info :category "check")
(set-info :status sat)
(declare-fun x () (_ BitVec 15))
(declare-fun y () (_ BitVec 15))
(declare-fun z () (_ BitVec 15))



(assert (= y (bvadd x (_ bv1 15))))
(assert (= z (bvadd y (_ bv1 15))))

(assert (= (bvmul y y )  (bvadd (_ bv1 15) (bvmul x z))))



; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(exit)

