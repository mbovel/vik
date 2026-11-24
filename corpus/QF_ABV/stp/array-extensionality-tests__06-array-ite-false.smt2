; From stp: array-extensionality-tests/06-array-ite-false.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; CHECK-NEXT: ^unsat
; An array-valued if-then-else, false branch.
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 2) (_ BitVec 2)))
(declare-fun b () (Array (_ BitVec 2) (_ BitVec 2)))
(declare-fun c () (Array (_ BitVec 2) (_ BitVec 2)))
(declare-fun p () Bool)
(declare-fun i () (_ BitVec 2))
(assert (not p))
(assert (= (ite p a b) c))
(assert (distinct (select b i) (select c i)))
(set-info :status unsat)
(check-sat)
