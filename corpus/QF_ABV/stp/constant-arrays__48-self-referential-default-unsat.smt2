; From stp: constant-arrays/48-self-referential-default-unsat.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=on %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=off %s | %OutputCheck %s
; The occurs check must follow the hidden default: substituting this equality
; would erase the impossible requirement that a[i] equals a[i] + 1.
(set-logic QF_ABV)
(declare-fun a () (Array (_ BitVec 8) (_ BitVec 8)))
(declare-fun i () (_ BitVec 8))
(assert (= a ((as const (Array (_ BitVec 8) (_ BitVec 8)))
             (bvadd (select a i) #x01))))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
