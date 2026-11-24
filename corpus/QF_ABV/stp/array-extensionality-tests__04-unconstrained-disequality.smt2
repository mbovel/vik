; From stp: array-extensionality-tests/04-unconstrained-disequality.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; CHECK-NEXT: ^sat
; An unconstrained whole-array disequality is satisfiable via the
; witness index introduced for it.
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 2) (_ BitVec 2)))
(declare-fun b () (Array (_ BitVec 2) (_ BitVec 2)))
(assert (distinct a b))
(set-info :status sat)
(check-sat)
