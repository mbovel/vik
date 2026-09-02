; From smt-lib: QF_BV/bench_ab/a486test0024.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
Bit-vector benchmarks from Dawson Engler's tool contributed by Vijay Ganesh
(vganesh@stanford.edu).  Translated into SMT-LIB format by Clark Barrett using
CVC3.

|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun a_0 () (_ BitVec 32))
(declare-fun a_1 () (_ BitVec 32))
(declare-fun a_2 () (_ BitVec 32))
(assert (not (bvslt a_1 a_0)))
(assert (bvslt a_2 a_1))
(assert (bvslt a_2 a_0))
(assert (not (bvslt (_ bv10 32) a_1)))
(check-sat)
(exit)
