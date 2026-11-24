; From stp: smt2-command-tests/reset-assertions-drops-pushed-levels.smt2 (MIT, LICENSES/stp.txt)
; Assertion levels above the base one are removed too.
; RUN: %solver %s | %OutputCheck %s
(set-logic QF_BV)
(declare-fun x () (_ BitVec 4))
(assert (= x #x1))
(push 1)
(assert (= x #x2))
; CHECK-NEXT: ^unsat
(set-info :status unsat)
(check-sat)
(reset-assertions)
(declare-fun x () (_ BitVec 4))
(assert (= x #x3))
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
