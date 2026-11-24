; From stp: sample-smt-tests/letterm.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
; logic: QF_BV, the smallest the script fits, for QF_ABV (prune logic)
(set-logic QF_BV)
(set-info :smt-lib-version 2.0)

(assert
 (= (_ bv0 8)
    (let ((?x (_ bv0 8)))
         ?x)))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(exit)
