; From stp: smt2-command-tests/global-declarations-true-nested-levels.smt2 (MIT, LICENSES/stp.txt)
; Levels declared at different depths, and a multi-level pop, must all leave the
; declarations behind. A pop of several levels at once is the case where a
; per-level symbol list is easiest to unwind too far.
; RUN: %solver %s | %OutputCheck %s
(set-option :global-declarations true)
(set-logic QF_BV)
(declare-fun base () (_ BitVec 4))
(push 2)
(declare-fun outer () (_ BitVec 4))
(push 1)
(declare-fun inner () (_ BitVec 4))
(assert (= inner #x3))
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
(pop 3)
(assert (and (= base #x1) (= outer #x2) (= inner #x3)))
; CHECK-NEXT: ^sat$
(set-info :status sat)
(check-sat)
