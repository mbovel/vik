; From stp: incremental-tests/subst-restore-auto-engagement.smt2 (MIT, LICENSES/stp.txt)
; The same eliminated-definition restoration as
; subst-frozen-rhs-restores-eliminated.smt2, reached with no flags at all:
; a push makes the session incremental, the (unknown-logic) policy engages
; the driver at the third real solve, and the base then grows a frozen
; definition whose right-hand side names an eliminated variable. This is
; the default-configuration route to the soundness hazard, so it is pinned
; separately from the forced --incremental one.
; RUN: %solver %s | %OutputCheck %s
; RUN: %solver --check-sanity %s | %OutputCheck %s
; logic: QF_BV, the smallest the script fits, which set none (prune logic)
(set-logic QF_BV)
(declare-fun y () (_ BitVec 8))
(push 1)
(pop 1)
(assert (bvult y #x10))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
(assert (bvult y #x0e))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
; The third real solve runs on the persistent driver and bit-blasts y.
(assert (bvult y #x0d))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
; x is eliminated by substitution; y's late definition is frozen, encoded
; raw, and mentions x. y < 13, y = x, x = 255: unsatisfiable.
(declare-fun x () (_ BitVec 8))
(assert (= x #xff))
(assert (= y x))
; CHECK-NEXT: ^unsat
(set-info :status unsat)
(check-sat)
(exit)
