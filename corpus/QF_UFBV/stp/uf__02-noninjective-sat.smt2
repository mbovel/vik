; From stp: uf/02-noninjective-sat.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --uninterpreted-functions --incremental=off %s 2>&1 | %OutputCheck %s
; RUN: %solver --uninterpreted-functions --incremental=on %s 2>&1 | %OutputCheck %s
; CHECK: ^sat
; CHECK: define-fun \|f\|
;
; EXPECT: sat
; EXPECT: ((define-fun x () (_ BitVec 4) #b0001) (define-fun y () (_ BitVec 4) #b0000) (define-fun f ((x!0 (_ BitVec 4))) (_ BitVec 8) (ite (= x!0 #b0000) #b00000000 (ite (= x!0 #b0001) #b00000000 #b00000000))))
(set-logic QF_UFBV)
(set-option :produce-models true)
(declare-fun x () (_ BitVec 4))
(declare-fun y () (_ BitVec 4))
(declare-fun f ((_ BitVec 4)) (_ BitVec 8))
(assert (distinct x y))
(assert (= (f x) (f y)))
(set-info :status sat)
(check-sat)
(get-model)
