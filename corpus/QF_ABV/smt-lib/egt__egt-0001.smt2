; From smt-lib: QF_ABV/egt/egt-0001.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_ABV)
(set-info :source |
Bit-vector benchmarks from Dawson Engler's tool contributed by Vijay Ganesh
(vganesh@stanford.edu).  Translated into SMT-LIB format by Clark Barrett using
CVC3.

|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun packet () (Array (_ BitVec 32) (_ BitVec 8)))
(assert (= (concat (_ bv0 24) (select packet (_ bv240 32))) (_ bv53 32)))
(check-sat)
(exit)
