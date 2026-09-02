; From smt-lib: QF_BV/sage/app7/bench_3577.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
 Patrice Godefroid, SAGE (systematic dynamic test generation)
 For more information: http://research.microsoft.com/en-us/um/people/pg/public_psfiles/ndss2008.pdf
|)
(set-info :category "industrial")
(set-info :status unsat)
(declare-fun T1_5614 () (_ BitVec 8))
(assert (and true (= T1_5614 (_ bv0 8)) (= T1_5614 (_ bv112 8))))
(check-sat)
(exit)
