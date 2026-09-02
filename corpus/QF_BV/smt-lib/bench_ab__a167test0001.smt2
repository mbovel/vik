; From smt-lib: QF_BV/bench_ab/a167test0001.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
Bit-vector benchmarks from Dawson Engler's tool contributed by Vijay Ganesh
(vganesh@stanford.edu).  Translated into SMT-LIB format by Clark Barrett using
CVC3.

|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun n () (_ BitVec 32))
(declare-fun c () (_ BitVec 8))
(assert (bvult n (_ bv16 32)))
(assert (not (not (= (concat (_ bv0 24) c) (_ bv254 32)))))
(check-sat)
(exit)
