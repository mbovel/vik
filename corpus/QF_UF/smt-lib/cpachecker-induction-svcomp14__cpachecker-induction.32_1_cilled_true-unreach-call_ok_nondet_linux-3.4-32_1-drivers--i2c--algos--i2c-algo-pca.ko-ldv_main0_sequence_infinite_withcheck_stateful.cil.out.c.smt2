; From smt-lib: QF_UFLRA/cpachecker-induction-svcomp14/cpachecker-induction.32_1_cilled_true-unreach-call_ok_nondet_linux-3.4-32_1-drivers--i2c--algos--i2c-algo-pca.ko-ldv_main0_sequence_infinite_withcheck_stateful.cil.out.c.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
; logic: QF_UF, the smallest the script fits, for QF_UFLRA (prune logic)
(set-info :smt-lib-version 2.6)
(set-logic QF_UF)
(set-info :source |CPAchecker with k-induction on SV-COMP14 program using MathSAT5, submitted by Philipp Wendler, http://cpachecker.sosy-lab.org|)
(set-info :category "industrial")
(set-info :status unsat)


(define-fun _1 () Bool true)
(define-fun _2 () Bool false)



(assert _1)

(assert _2)
(check-sat)


(exit)
