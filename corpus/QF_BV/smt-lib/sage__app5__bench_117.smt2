; From smt-lib: QF_BV/sage/app5/bench_117.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
 Patrice Godefroid, SAGE (systematic dynamic test generation)
 For more information: http://research.microsoft.com/en-us/um/people/pg/public_psfiles/ndss2008.pdf
|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun T1_2115561 () (_ BitVec 8))
(assert (and true (not (= (bvand T1_2115561 (_ bv16 8)) (_ bv0 8)))))
(check-sat)
(exit)
