; From smt-lib: QF_BV/crafted/bv8.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
Hand-crafted bit-vector benchmarks.  Some are from the SVC benchmark suite.
Contributed by Vijay Ganesh (vganesh@stanford.edu).  Translated into SMT-LIB
format by Clark Barrett using CVC3.

|)
(set-info :category "crafted")
(set-info :status unsat)
(assert (let ((?v_0 (= ((_ extract 0 0) (_ bv2 3)) (_ bv1 1)))) (not (= (ite (not (= (ite (= (ite (not (or ?v_0 (and (and (= ((_ extract 0 0) (_ bv5 3)) (_ bv1 1)) (not ?v_0)) (= (_ bv0 1) (_ bv1 1))))) (_ bv1 1) (_ bv0 1)) (_ bv1 1)) (_ bv1 1) (_ bv0 1)) (_ bv1 1))) (_ bv1 1) (_ bv0 1)) (_ bv0 1)))))
(check-sat)
(exit)
