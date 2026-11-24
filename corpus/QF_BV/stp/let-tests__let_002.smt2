; From stp: let-tests/let_002.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :status sat)
(declare-fun u () (_ BitVec 1))
(assert
 (let ((v (_ bv0 1)))
 (let ((v (_ bv0 1)))
 (= (_ bv0 1) (_ bv0 1)))))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(exit)
