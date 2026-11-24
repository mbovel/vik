; From stp: smt2-command-tests/boolean-arrays-model.smt2 (MIT, LICENSES/stp.txt)
; Boolean cells and indices print as true/false, including completed arrays.
; RUN: %solver %s | %OutputCheck %s
; RUN: %solver --incremental=off %s | %OutputCheck %s
; RUN: %solver --array-ackermann-budget=0 %s | %OutputCheck %s
; logic: QF_AX, the smallest the script fits, for ALL (prune logic)
(set-option :produce-models true)
(set-logic QF_AX)
(declare-const a (Array Bool Bool))
(declare-const p Bool)
(assert (select a false))
(assert (not (select a true)))
(assert p)
; CHECK-NEXT: ^sat$
(set-info :status sat)
(check-sat)
; CHECK: \(select a false\).*true
; CHECK-NEXT: \(select a true\).*false
; CHECK-NEXT: \(select a p\).*false
(get-value ((select a false) (select a true) (select a p)))
; CHECK: \(define-fun \|a\| \(\) \(Array Bool Bool\)
; CHECK-NOT: #b
(get-model)
(push 1)
(assert (not p))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
(pop 1)
; CHECK-NEXT: ^sat$
(set-info :status sat)
(check-sat)
; CHECK: \(select a false\).*true
(get-value ((select a false)))
