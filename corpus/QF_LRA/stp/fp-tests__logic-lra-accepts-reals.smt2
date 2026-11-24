; From stp: fp-tests/logic-lra-accepts-reals.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
; CHECK-NEXT: ^sat
;
; The LRA variants of the floating-point logics are the floating-point
; logics plus the theory of reals: Real declarations and linear arithmetic
; are part of them.
; logic: QF_LRA, the smallest the script fits, for QF_BVFPLRA (prune logic)
(set-logic QF_LRA)
(declare-fun r () Real)
(assert (> r 0.0))
(set-info :status sat)
(check-sat)
