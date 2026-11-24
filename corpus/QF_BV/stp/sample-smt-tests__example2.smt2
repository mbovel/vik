; From stp: sample-smt-tests/example2.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
; CHECK-NEXT: ^sat
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

(assert
 (let ((?x symb_1_179))
  (bvuge
    (let ((?y symb_1_179))
      ?y)
    ?x
)))

(set-info :status sat)
(check-sat)
(exit)
