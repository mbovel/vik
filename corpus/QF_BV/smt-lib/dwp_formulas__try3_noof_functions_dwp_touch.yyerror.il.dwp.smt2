; From smt-lib: QF_BV/dwp_formulas/try3_noof_functions_dwp_touch.yyerror.il.dwp.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
Ivan Jager <aij+nospam@andrew.cmu.edu>

|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun t_124 () (_ BitVec 1))
(declare-fun t_123 () (_ BitVec 1))
(assert (= (_ bv1 1) (bvand (bvand (ite (= t_123 (_ bv1 1)) (_ bv1 1) (_ bv0 1)) (ite (= t_124 t_123) (_ bv1 1) (_ bv0 1))) (bvand (bvnot (bvnot t_123)) (bvand t_124 (_ bv1 1))))))
(check-sat)
(exit)
