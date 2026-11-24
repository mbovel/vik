; From stp: smt2-command-tests/global-declarations-true-reset-assertions.smt2 (MIT, LICENSES/stp.txt)
; reset-assertions keeps the declarations when :global-declarations is true and
; only empties the assertion stack (SMT-LIB 2.6, section 4.2.4). This is the
; shape an interactive driver wants: stream a large term once, then re-query it
; against different assertions without redeclaring anything.
; RUN: %solver %s | %OutputCheck %s
(set-option :global-declarations true)
; get-value below needs a model to have been asked for; see
; global-declarations-false-redeclare-after-pop.
(set-option :produce-models true)
(set-logic QF_BV)
(declare-fun x () (_ BitVec 3))
(assert (= x #b010))
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
(assert (= x #b001))
; CHECK-NEXT: ^unsat$
(set-info :status unsat)
(check-sat)
(reset-assertions)
(assert (= x #b011))
; CHECK-NEXT: ^sat$
(set-info :status sat)
(check-sat)
; CHECK-NEXT: ^\($
; CHECK-NEXT: .*x.*#b011.*
; CHECK-NEXT: ^\)$
(get-value (x))
