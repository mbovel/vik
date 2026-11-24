; From stp: constant-arrays/01-read-is-the-default.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
; CHECK-NEXT: ^unsat
; A read of a constant array is its default at every index: the read folds
; at construction, so no array machinery is needed to decide this.
(set-logic QF_ABV)
(declare-fun i () (_ BitVec 8))
(assert (not (= (select ((as const (Array (_ BitVec 8) (_ BitVec 8))) #x07) i) #x07)))
(set-info :status unsat)
(check-sat)
