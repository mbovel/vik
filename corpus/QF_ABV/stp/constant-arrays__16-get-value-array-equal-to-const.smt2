; From stp: constant-arrays/16-get-value-array-equal-to-const.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; CHECK-NEXT: ^sat
; CHECK: #x07
; The model of an array equated with a constant array: an unobserved cell
; reads the default.
(set-option :produce-models true)
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 8) (_ BitVec 8)))
(assert (= a ((as const (Array (_ BitVec 8) (_ BitVec 8))) #x07)))
(set-info :status sat)
(check-sat)
(get-value ((select a #x00)))
