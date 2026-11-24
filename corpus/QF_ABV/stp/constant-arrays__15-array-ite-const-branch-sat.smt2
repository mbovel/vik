; From stp: constant-arrays/15-array-ite-const-branch-sat.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=on %s | %OutputCheck %s
; CHECK-NEXT: ^sat
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 8) (_ BitVec 8)))
(declare-fun d () (Array (_ BitVec 8) (_ BitVec 8)))
(declare-fun b () Bool)
(assert (= a (ite b ((as const (Array (_ BitVec 8) (_ BitVec 8))) #x01) d)))
(assert b)
(assert (= (select a #x02) #x01))
(set-info :status sat)
(check-sat)
