; From stp: lra-real-distinct-equal-constants.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --SMTLIB2 %s | %OutputCheck %s
;
; The other half of the same case: operands that settle as equal make the
; disequality false, so the assertion is unsatisfiable.
(set-logic QF_LRA)
(assert (distinct 0.0 0.0))
; CHECK: ^unsat
(set-info :status unsat)
(check-sat)
