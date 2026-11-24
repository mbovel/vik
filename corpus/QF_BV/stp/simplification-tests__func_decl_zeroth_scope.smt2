; From stp: simplification-tests/func_decl_zeroth_scope.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --exit-after-CNF %s | %OutputCheck %s
; logic: QF_BV, the smallest the script fits, for QF_ABV (prune logic)
(set-logic QF_BV)
(define-fun |a| () Bool true)
(assert |a|)
(push 1)
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(pop 1)
(exit)
