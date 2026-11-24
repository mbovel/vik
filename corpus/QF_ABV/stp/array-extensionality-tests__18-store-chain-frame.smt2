; From stp: array-extensionality-tests/18-store-chain-frame.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; CHECK-NEXT: ^unsat
; A store chain equated with its own base forces each written value to
; be in the base already: read(a,i) must equal v.
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 4) (_ BitVec 8)))
(declare-fun i () (_ BitVec 4))
(declare-fun v () (_ BitVec 8))
(assert (= (store a i v) a))
(assert (distinct (select a i) v))
(set-info :status unsat)
(check-sat)
