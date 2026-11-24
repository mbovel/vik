; From stp: smt2-command-tests/global-declarations-set-before-declarations.smt2 (MIT, LICENSES/stp.txt)
; The other side of global-declarations-set-late-is-refused: what makes the
; option too late to set is a declaration or an assertion, not merely a
; command. set-logic, set-info and other options do not close the window, and
; reset re-opens it -- there are no declarations left to be ambiguous about.
; RUN: %solver %s | %OutputCheck %s
(set-logic QF_BV)
(set-info :source "STP option-ordering test")
(set-option :produce-models true)
(set-option :global-declarations true)
(push 1)
(declare-fun x () (_ BitVec 4))
(pop 1)
(assert (= x #x1))
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
(reset)
(set-logic QF_BV)
(set-option :global-declarations true)
(push 1)
(declare-fun y () (_ BitVec 4))
(pop 1)
(assert (= y #x2))
; CHECK-NEXT: ^sat$
(set-info :status sat)
(check-sat)
