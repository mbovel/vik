; From stp: constant-arrays/12-equal-consts-same-default.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; CHECK-NEXT: ^sat
; Two constant arrays whose defaults are the same value are one array, a
; ground default that folds to it included.
(set-logic QF_ABV)
(assert (= ((as const (Array (_ BitVec 8) (_ BitVec 8))) (bvadd #x02 #x03)) ((as const (Array (_ BitVec 8) (_ BitVec 8))) #x05)))
(set-info :status sat)
(check-sat)
