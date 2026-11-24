; From stp: constant-arrays/52-pushed-default-restores-definition.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=on %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=off %s | %OutputCheck %s
; A base definition may be eliminated by the first check. A later use
; hidden in a default must restore it before the source symbol is encoded.
(set-logic QF_ABV)
(declare-fun x () (_ BitVec 8))
(declare-fun b () Bool)
(declare-fun i () (_ BitVec 8))
(declare-fun j () (_ BitVec 8))
(assert (= x #x03))
(assert b)
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
(push 1)
(assert (= (select (store ((as const (Array (_ BitVec 8) (_ BitVec 8))) x)
                          i #x00)
                   j)
           #x04))
(assert (distinct i j))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
(pop 1)
(push 1)
(assert (= (select (store ((as const (Array (_ BitVec 8) (_ BitVec 8)))
                           (ite b #x03 #x04))
                          i #x00)
                   j)
           #x04))
(assert (distinct i j))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
(pop 1)
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
