; From stp: array-extensionality-tests/19-store-chain-frame-sat.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; CHECK-NEXT: ^sat
; The satisfiable side of the frame condition: the base may already
; hold the written value.
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 4) (_ BitVec 8)))
(declare-fun i () (_ BitVec 4))
(declare-fun v () (_ BitVec 8))
(assert (= (store a i v) a))
(assert (= (select a i) v))
(set-info :status sat)
(check-sat)
