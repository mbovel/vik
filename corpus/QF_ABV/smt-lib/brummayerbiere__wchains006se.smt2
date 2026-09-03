; From smt-lib: QF_ABV/brummayerbiere/wchains006se.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_ABV)
(set-info :source |
This benchmark generates write chain permutations and tries to show
via extensionality that they are equal.

Contributed by Armin Biere (armin.biere@jku.at).
|)
(set-info :category "crafted")
(set-info :status sat)
(declare-fun a1 () (Array (_ BitVec 32) (_ BitVec 8)))
(declare-fun v6 () (_ BitVec 32))
(declare-fun v7 () (_ BitVec 32))
(declare-fun v8 () (_ BitVec 32))
(declare-fun v9 () (_ BitVec 32))
(declare-fun v10 () (_ BitVec 32))
(declare-fun v11 () (_ BitVec 32))
(assert (let ((?v_40 (bvadd (_ bv0 32) v6)) (?v_41 ((_ extract 7 0) v6)) (?v_42 (bvadd (_ bv1 32) v6)) (?v_43 ((_ extract 15 8) v6)) (?v_44 (bvadd (_ bv2 32) v6)) (?v_45 ((_ extract 23 16) v6)) (?v_46 (bvadd (_ bv3 32) v6)) (?v_47 ((_ extract 31 24) v6)) (?v_32 (bvadd (_ bv0 32) v7)) (?v_33 ((_ extract 7 0) v7)) (?v_34 (bvadd (_ bv1 32) v7)) (?v_35 ((_ extract 15 8) v7)) (?v_36 (bvadd (_ bv2 32) v7)) (?v_37 ((_ extract 23 16) v7)) (?v_38 (bvadd (_ bv3 32) v7)) (?v_39 ((_ extract 31 24) v7)) (?v_24 (bvadd (_ bv0 32) v8)) (?v_25 ((_ extract 7 0) v8)) (?v_26 (bvadd (_ bv1 32) v8)) (?v_27 ((_ extract 15 8) v8)) (?v_28 (bvadd (_ bv2 32) v8)) (?v_29 ((_ extract 23 16) v8)) (?v_30 (bvadd (_ bv3 32) v8)) (?v_31 ((_ extract 31 24) v8)) (?v_16 (bvadd (_ bv0 32) v9)) (?v_17 ((_ extract 7 0) v9)) (?v_18 (bvadd (_ bv1 32) v9)) (?v_19 ((_ extract 15 8) v9)) (?v_20 (bvadd (_ bv2 32) v9)) (?v_21 ((_ extract 23 16) v9)) (?v_22 (bvadd (_ bv3 32) v9)) (?v_23 ((_ extract 31 24) v9)) (?v_8 (bvadd (_ bv0 32) v10)) (?v_9 ((_ extract 7 0) v10)) (?v_10 (bvadd (_ bv1 32) v10)) (?v_11 ((_ extract 15 8) v10)) (?v_12 (bvadd (_ bv2 32) v10)) (?v_13 ((_ extract 23 16) v10)) (?v_14 (bvadd (_ bv3 32) v10)) (?v_15 ((_ extract 31 24) v10)) (?v_0 (bvadd (_ bv0 32) v11)) (?v_1 ((_ extract 7 0) v11)) (?v_2 (bvadd (_ bv1 32) v11)) (?v_3 ((_ extract 15 8) v11)) (?v_4 (bvadd (_ bv2 32) v11)) (?v_5 ((_ extract 23 16) v11)) (?v_6 (bvadd (_ bv3 32) v11)) (?v_7 ((_ extract 31 24) v11))) (not (= (bvnot (ite (= (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store a1 ?v_40 ?v_41) ?v_42 ?v_43) ?v_44 ?v_45) ?v_46 ?v_47) ?v_32 ?v_33) ?v_34 ?v_35) ?v_36 ?v_37) ?v_38 ?v_39) ?v_24 ?v_25) ?v_26 ?v_27) ?v_28 ?v_29) ?v_30 ?v_31) ?v_16 ?v_17) ?v_18 ?v_19) ?v_20 ?v_21) ?v_22 ?v_23) ?v_8 ?v_9) ?v_10 ?v_11) ?v_12 ?v_13) ?v_14 ?v_15) ?v_0 ?v_1) ?v_2 ?v_3) ?v_4 ?v_5) ?v_6 ?v_7) (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store (store a1 ?v_0 ?v_1) ?v_2 ?v_3) ?v_4 ?v_5) ?v_6 ?v_7) ?v_8 ?v_9) ?v_10 ?v_11) ?v_12 ?v_13) ?v_14 ?v_15) ?v_16 ?v_17) ?v_18 ?v_19) ?v_20 ?v_21) ?v_22 ?v_23) ?v_24 ?v_25) ?v_26 ?v_27) ?v_28 ?v_29) ?v_30 ?v_31) ?v_32 ?v_33) ?v_34 ?v_35) ?v_36 ?v_37) ?v_38 ?v_39) ?v_40 ?v_41) ?v_42 ?v_43) ?v_44 ?v_45) ?v_46 ?v_47)) (_ bv1 1) (_ bv0 1))) (_ bv0 1)))))
(check-sat)
(exit)
