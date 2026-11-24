; From stp: uf/mode-equivalence/core-congruence.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --uninterpreted-functions --incremental=off %s 2>&1 | %OutputCheck %s
; RUN: %solver --uninterpreted-functions --incremental=on %s 2>&1 | %OutputCheck %s
; CHECK: ^unsat
;
(set-logic QF_UFBV)
(declare-fun f ((_ BitVec 8) Bool) (_ BitVec 4))
(declare-const x (_ BitVec 8))
(declare-const y (_ BitVec 8))
(assert (= x y))
(assert (distinct (f x true) (f y true)))
(set-info :status unsat)
(check-sat) ; unsat in batch and persistent
