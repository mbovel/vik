; From stp: sample-smt-tests/nestedlet.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
; logic: QF_BV, the smallest the script fits, for QF_ABV (prune logic)
(set-logic QF_BV)
(set-info :smt-lib-version 2.0)
(declare-fun symb_1_179 () (_ BitVec 8))
 
(assert
 (let ((?x true))
  (and
    (let ((?y true))
      ?y)
    ?x
)))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(exit)
