; From stp: uf/frontend-golden/define-fun-bool-formal-shadows-uf.smt2 (MIT, LICENSES/stp.txt)
; RUN: %solver --uninterpreted-functions --incremental=off %s 2>&1 | %OutputCheck %s
; RUN: %solver --uninterpreted-functions --incremental=on %s 2>&1 | %OutputCheck %s
; CHECK-NEXT: ^unsat
; CHECK-NEXT: ^"REACHED-END"
;
; Boolean formals obey the same binder-before-global-namespace rule as
; bit-vector formals.
; logic: QF_UF, the smallest the script fits, for QF_UFBV (prune logic)
(set-logic QF_UF)
(declare-fun predicate (Bool) Bool)
(define-fun identity ((predicate Bool)) Bool predicate)
(assert (not (identity true)))
(set-info :status unsat)
(check-sat)
(echo "REACHED-END")
