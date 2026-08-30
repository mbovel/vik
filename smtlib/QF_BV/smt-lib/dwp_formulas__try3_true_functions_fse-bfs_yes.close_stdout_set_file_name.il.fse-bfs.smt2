; From smt-lib: QF_BV/dwp_formulas/try3_true_functions_fse-bfs_yes.close_stdout_set_file_name.il.fse-bfs.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
Ivan Jager <aij+nospam@andrew.cmu.edu>

|)
(set-info :category "industrial")
(set-info :status sat)
(assert (= (_ bv1 1) (bvand (_ bv1 1) (_ bv1 1))))
(check-sat)
(exit)
