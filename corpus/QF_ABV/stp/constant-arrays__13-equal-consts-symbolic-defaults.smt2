; From stp: constant-arrays/13-equal-consts-symbolic-defaults.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=on %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=off %s | %OutputCheck %s
; CHECK-NEXT: ^sat$
; CHECK-NEXT: ^unsat$
; CHECK-NEXT: ^sat$
; Equality of constant arrays with symbolic defaults forces those defaults
; equal. The defaults remain visible after a contradictory scope is popped.
(set-logic QF_ABV)
(declare-fun v () (_ BitVec 8))
(declare-fun w () (_ BitVec 8))
(assert (= ((as const (Array (_ BitVec 8) (_ BitVec 8))) v) ((as const (Array (_ BitVec 8) (_ BitVec 8))) w)))
(set-info :status sat)
(check-sat)
(push 1)
(assert (not (= v w)))
(set-info :status unsat)
(check-sat)
(pop 1)
(set-info :status sat)
(check-sat)
