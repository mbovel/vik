; From stp: constant-arrays/42-symbolic-default-reads-another-array.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --array-equality %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=on %s | %OutputCheck %s
; RUN: %solver --array-equality --incremental=off %s | %OutputCheck %s
; A read hidden inside a constant-array default must participate in solving
; and model evaluation, including after a contradictory scope is popped.
(set-option :produce-models true)
(set-logic QF_ABV)
(declare-fun src () (Array (_ BitVec 8) (_ BitVec 8)))
(declare-fun dst () (Array (_ BitVec 8) (_ BitVec 8)))
(declare-fun i () (_ BitVec 8))
(assert (= dst ((as const (Array (_ BitVec 8) (_ BitVec 8)))
               (bvadd (select src i) #x01))))
(assert (= (select src i) #x06))
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
; CHECK: #x07
(get-value ((select dst #x02)))
(push 1)
(assert (not (= (select dst #x02) #x07)))
; CHECK: ^unsat$
(set-info :status unsat)
(check-sat)
(pop 1)
; CHECK: ^sat$
(set-info :status sat)
(check-sat)
; CHECK: #x07
(get-value ((select dst #x02)))
