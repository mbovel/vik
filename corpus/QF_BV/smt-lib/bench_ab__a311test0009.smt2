; From smt-lib: QF_BV/bench_ab/a311test0009.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_BV)
(set-info :source |
Bit-vector benchmarks from Dawson Engler's tool contributed by Vijay Ganesh
(vganesh@stanford.edu).  Translated into SMT-LIB format by Clark Barrett using
CVC3.

|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun utf8_0 () (_ BitVec 8))
(assert (not (bvslt (concat (_ bv0 24) ((_ extract 7 0) utf8_0)) (_ bv128 32))))
(assert (not (bvslt (concat (_ bv0 24) ((_ extract 7 0) utf8_0)) (_ bv194 32))))
(assert (not (bvslt (concat (_ bv0 24) ((_ extract 7 0) utf8_0)) (_ bv224 32))))
(assert (not (bvslt (concat (_ bv0 24) ((_ extract 7 0) utf8_0)) (_ bv240 32))))
(assert (bvslt (concat (_ bv0 24) ((_ extract 7 0) utf8_0)) (_ bv248 32)))
(check-sat)
(exit)
