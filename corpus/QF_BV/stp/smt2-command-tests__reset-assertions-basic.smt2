; From stp: smt2-command-tests/reset-assertions-basic.smt2 (MIT, LICENSES/stp.txt)
; reset-assertions must empty the assertion stack, so an unsatisfiable
; conjunction stops being unsatisfiable afterwards.
; RUN: %solver %s | %OutputCheck %s
(set-logic QF_BV)
(declare-fun x () (_ BitVec 4))
(assert (and (= x #x1) (= x #x2)))
; CHECK-NEXT: ^unsat
(set-info :status unsat)
(check-sat)
(reset-assertions)
; CHECK-NEXT: ^sat
(set-info :status sat)
(check-sat)
