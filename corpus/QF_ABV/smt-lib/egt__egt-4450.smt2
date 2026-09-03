; From smt-lib: QF_ABV/egt/egt-4450.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
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
(assert (not (= (concat (_ bv0 24) (select packet (_ bv240 32))) (_ bv53 32))))
(assert (not (= (concat (_ bv0 24) (select packet (_ bv240 32))) (_ bv0 32))))
(assert (not (= (concat (_ bv0 24) (select packet (_ bv240 32))) (_ bv52 32))))
(assert (not (= (concat (_ bv0 24) (select packet (_ bv240 32))) (_ bv255 32))))
(assert (= (concat (_ bv0 24) (select packet (bvadd (_ bv242 32) (concat (_ bv0 24) (select packet (_ bv241 32)))))) (_ bv53 32)))
(assert (not (bvsle (_ bv308 32) (bvadd (bvadd (bvadd (_ bv0 32) (bvadd (concat (_ bv0 24) (select packet (_ bv241 32))) (_ bv2 32))) (_ bv1 32)) (concat (_ bv0 24) (_ bv83 8))))))
(assert (not (= ((_ extract 31 0) (bvadd (bvadd (_ bv4277102048 32) (bvadd (_ bv0 32) (bvadd (concat (_ bv0 24) (select packet (_ bv241 32))) (_ bv2 32)))) (_ bv2 32))) ((_ extract 31 0) (_ bv0 32)))))
(check-sat)
(exit)
