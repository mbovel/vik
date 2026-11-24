; From stp: incremental-tests/engagement-two-checks.smt2 (MIT, LICENSES/stp.txt)
; A two-check-sat session never engages the incremental driver: its
; second solve is its last, and the driver's persistent encoding can
; never be repaid with zero solves remaining (the campaign's loss tail
; was exactly these files). No driver stats line may appear anywhere in
; the session.
; RUN: %solver -s %s 2>&1 | %OutputCheck %s
(set-logic QF_BV)
(declare-fun x () (_ BitVec 8))
(assert (bvult x #x80))
(push 1)
(assert (bvult x #x40))
; CHECK-NOT: Incremental: encoded
; CHECK: ^sat
(set-info :status sat)
(check-sat)
(pop 1)
(push 1)
(assert (bvuge x #x80))
; CHECK-NOT: Incremental: encoded
; CHECK: ^unsat
(set-info :status unsat)
(check-sat)
; CHECK-NOT: Incremental: encoded
(pop 1)
(exit)
