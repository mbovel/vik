; From stp: constant-arrays/18-get-model-store-chain-over-const.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; CHECK-NEXT: ^sat
; CHECK: define-fun \|a\| .*as const.*#x07.*#x05 #x2A
; The model of an array equated with a store over a constant array: default
; #x07 underneath, the written cell on top.
(set-option :produce-models true)
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 8) (_ BitVec 8)))
(assert (= a (store ((as const (Array (_ BitVec 8) (_ BitVec 8))) #x07) #x05 #x2A)))
(set-info :status sat)
(check-sat)
(get-model)
