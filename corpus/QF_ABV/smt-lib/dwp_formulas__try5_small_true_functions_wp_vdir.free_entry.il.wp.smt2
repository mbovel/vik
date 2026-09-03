; From smt-lib: QF_ABV/dwp_formulas/try5_small_true_functions_wp_vdir.free_entry.il.wp.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_ABV)
(set-info :source |
Ivan Jager <aij+nospam@andrew.cmu.edu>

|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun mem_35_84 () (Array (_ BitVec 32) (_ BitVec 8)))
(declare-fun R_EDX_8_73 () (_ BitVec 32))
(declare-fun R_EAX_5_64 () (_ BitVec 32))
(declare-fun R_EBP_0_60 () (_ BitVec 32))
(declare-fun R_ESP_1_58 () (_ BitVec 32))
(assert (= (_ bv1 1) (_ bv1 1)))
(check-sat)
(exit)
