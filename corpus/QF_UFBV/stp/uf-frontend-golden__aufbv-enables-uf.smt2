; From stp: uf/frontend-golden/aufbv-enables-uf.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --incremental=off %s 2>&1 | %OutputCheck %s
; RUN: %solver --incremental=on %s 2>&1 | %OutputCheck %s
; CHECK-NOT: syntax error
; CHECK: ^unsat
;
; QF_AUFBV contains UF and therefore enables non-nullary functions itself.
; logic: QF_UFBV, the smallest the script fits, for QF_AUFBV (prune logic)
(set-logic QF_UFBV)
(declare-fun f ((_ BitVec 8)) (_ BitVec 8))
(assert (distinct (f #x00) (f #x00)))
(set-info :status unsat)
(check-sat)
