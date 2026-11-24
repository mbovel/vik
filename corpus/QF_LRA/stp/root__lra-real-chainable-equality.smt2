; From stp: lra-real-chainable-equality.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver %s | %OutputCheck %s
; Core equality is chainable for every sort, including mathematical Real.
(set-logic QF_LRA)
(declare-const x Real)
(declare-const y Real)
(declare-const z Real)
(assert (= x y z 1.0))
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
(push 1)
(assert (> y 1.0))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
(pop 1)
(assert (not (= 1.0 x y z)))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
