; From smt-lib: QF_ABV/platania/copy_array/copy_array6.c.smt2 (CC BY 4.0 (SMT-LIB release 2025, non-incremental, Zenodo 16740866))
(set-info :smt-lib-version 2.6)
(set-logic QF_ABV)
(set-info :source |
The benchmarks come from Bounded Model Checking of software. Each of them is a
family of formulae obtained during the check of a program and is parametric in
the size of the arrays. They actually don't involve Uninterpreted Functions but
just Arrays and Bitvectors.

This family contains formulae modeling a program that copies one array of size
2N into another one of size N (the increasing parameter).

Contributed by Lorenzo Platania (c1009@unige.it).  Translated from CVC format
by Clark Barrett.

|)
(set-info :category "industrial")
(set-info :status sat)
(declare-fun main_Q_0_Q_i_Q_0 () (_ BitVec 32))
(declare-fun main_Q_0_Q_i_Q_1 () (_ BitVec 32))
(assert (= main_Q_0_Q_i_Q_1 (_ bv0 32)))
(declare-fun main_Q_0_Q_b_Q_0 () (Array (_ BitVec 32) (_ BitVec 32)))
(declare-fun main_Q_0_Q_a_Q_0 () (Array (_ BitVec 32) (_ BitVec 32)))
(declare-fun main_Q_0_Q_a_Q_1 () (Array (_ BitVec 32) (_ BitVec 32)))
(assert (let ((?v_0 (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_1))) (= main_Q_0_Q_a_Q_1 (ite (not (= ?v_0 (_ bv0 32))) (store main_Q_0_Q_a_Q_0 main_Q_0_Q_i_Q_1 ?v_0) main_Q_0_Q_a_Q_0))))
(declare-fun main_Q_0_Q_temp_Q_i_Q_0 () (_ BitVec 32))
(declare-fun main_Q_0_Q_temp_Q_i_Q_1 () (_ BitVec 32))
(assert (= main_Q_0_Q_temp_Q_i_Q_1 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_1) (_ bv0 32))) main_Q_0_Q_i_Q_1 main_Q_0_Q_temp_Q_i_Q_0)))
(declare-fun main_Q_0_Q_i_Q_2 () (_ BitVec 32))
(assert (= main_Q_0_Q_i_Q_2 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_1) (_ bv0 32))) (bvadd main_Q_0_Q_i_Q_1 (_ bv1 32)) main_Q_0_Q_i_Q_1)))
(declare-fun main_Q_0_Q_a_Q_2 () (Array (_ BitVec 32) (_ BitVec 32)))
(assert (let ((?v_0 (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_2))) (= main_Q_0_Q_a_Q_2 (ite (not (= ?v_0 (_ bv0 32))) (store main_Q_0_Q_a_Q_1 main_Q_0_Q_i_Q_2 ?v_0) main_Q_0_Q_a_Q_1))))
(declare-fun main_Q_0_Q_temp_Q_i_Q_2 () (_ BitVec 32))
(assert (= main_Q_0_Q_temp_Q_i_Q_2 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_2) (_ bv0 32))) main_Q_0_Q_i_Q_2 main_Q_0_Q_temp_Q_i_Q_1)))
(declare-fun main_Q_0_Q_i_Q_3 () (_ BitVec 32))
(assert (= main_Q_0_Q_i_Q_3 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_2) (_ bv0 32))) (bvadd main_Q_0_Q_i_Q_2 (_ bv1 32)) main_Q_0_Q_i_Q_2)))
(declare-fun main_Q_0_Q_a_Q_3 () (Array (_ BitVec 32) (_ BitVec 32)))
(assert (let ((?v_0 (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_3))) (= main_Q_0_Q_a_Q_3 (ite (not (= ?v_0 (_ bv0 32))) (store main_Q_0_Q_a_Q_2 main_Q_0_Q_i_Q_3 ?v_0) main_Q_0_Q_a_Q_2))))
(declare-fun main_Q_0_Q_temp_Q_i_Q_3 () (_ BitVec 32))
(assert (= main_Q_0_Q_temp_Q_i_Q_3 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_3) (_ bv0 32))) main_Q_0_Q_i_Q_3 main_Q_0_Q_temp_Q_i_Q_2)))
(declare-fun main_Q_0_Q_i_Q_4 () (_ BitVec 32))
(assert (= main_Q_0_Q_i_Q_4 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_3) (_ bv0 32))) (bvadd main_Q_0_Q_i_Q_3 (_ bv1 32)) main_Q_0_Q_i_Q_3)))
(declare-fun main_Q_0_Q_a_Q_4 () (Array (_ BitVec 32) (_ BitVec 32)))
(assert (let ((?v_0 (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_4))) (= main_Q_0_Q_a_Q_4 (ite (not (= ?v_0 (_ bv0 32))) (store main_Q_0_Q_a_Q_3 main_Q_0_Q_i_Q_4 ?v_0) main_Q_0_Q_a_Q_3))))
(declare-fun main_Q_0_Q_temp_Q_i_Q_4 () (_ BitVec 32))
(assert (= main_Q_0_Q_temp_Q_i_Q_4 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_4) (_ bv0 32))) main_Q_0_Q_i_Q_4 main_Q_0_Q_temp_Q_i_Q_3)))
(declare-fun main_Q_0_Q_i_Q_5 () (_ BitVec 32))
(assert (= main_Q_0_Q_i_Q_5 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_4) (_ bv0 32))) (bvadd main_Q_0_Q_i_Q_4 (_ bv1 32)) main_Q_0_Q_i_Q_4)))
(declare-fun main_Q_0_Q_a_Q_5 () (Array (_ BitVec 32) (_ BitVec 32)))
(assert (let ((?v_0 (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_5))) (= main_Q_0_Q_a_Q_5 (ite (not (= ?v_0 (_ bv0 32))) (store main_Q_0_Q_a_Q_4 main_Q_0_Q_i_Q_5 ?v_0) main_Q_0_Q_a_Q_4))))
(declare-fun main_Q_0_Q_temp_Q_i_Q_5 () (_ BitVec 32))
(assert (= main_Q_0_Q_temp_Q_i_Q_5 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_5) (_ bv0 32))) main_Q_0_Q_i_Q_5 main_Q_0_Q_temp_Q_i_Q_4)))
(declare-fun main_Q_0_Q_i_Q_6 () (_ BitVec 32))
(assert (= main_Q_0_Q_i_Q_6 (ite (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_5) (_ bv0 32))) (bvadd main_Q_0_Q_i_Q_5 (_ bv1 32)) main_Q_0_Q_i_Q_5)))
(assert (let ((?v_1 (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_1) (_ bv0 32)))) (?v_4 (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_2) (_ bv0 32)))) (?v_7 (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_3) (_ bv0 32)))) (?v_10 (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_4) (_ bv0 32)))) (?v_13 (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_5) (_ bv0 32)))) (?v_0 (bvule (_ bv0 32) main_Q_0_Q_i_Q_1))) (let ((?v_2 (and ?v_0 (bvult main_Q_0_Q_i_Q_1 (_ bv12 32)))) (?v_3 (bvule (_ bv0 32) main_Q_0_Q_i_Q_2))) (let ((?v_5 (and ?v_3 (bvult main_Q_0_Q_i_Q_2 (_ bv12 32)))) (?v_6 (bvule (_ bv0 32) main_Q_0_Q_i_Q_3))) (let ((?v_8 (and ?v_6 (bvult main_Q_0_Q_i_Q_3 (_ bv12 32)))) (?v_9 (bvule (_ bv0 32) main_Q_0_Q_i_Q_4))) (let ((?v_11 (and ?v_9 (bvult main_Q_0_Q_i_Q_4 (_ bv12 32)))) (?v_12 (bvule (_ bv0 32) main_Q_0_Q_i_Q_5))) (let ((?v_14 (and ?v_12 (bvult main_Q_0_Q_i_Q_5 (_ bv12 32))))) (not (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and (and ?v_2 (=> ?v_1 (and ?v_0 (bvult main_Q_0_Q_i_Q_1 (_ bv6 32))))) (=> ?v_1 ?v_2)) (=> ?v_1 ?v_5)) (=> ?v_4 (and ?v_3 (bvult main_Q_0_Q_i_Q_2 (_ bv6 32))))) (=> ?v_4 ?v_5)) (=> ?v_4 ?v_8)) (=> ?v_7 (and ?v_6 (bvult main_Q_0_Q_i_Q_3 (_ bv6 32))))) (=> ?v_7 ?v_8)) (=> ?v_7 ?v_11)) (=> ?v_10 (and ?v_9 (bvult main_Q_0_Q_i_Q_4 (_ bv6 32))))) (=> ?v_10 ?v_11)) (=> ?v_10 ?v_14)) (=> ?v_13 (and ?v_12 (bvult main_Q_0_Q_i_Q_5 (_ bv6 32))))) (=> ?v_13 ?v_14)) (=> ?v_13 (and (bvule (_ bv0 32) main_Q_0_Q_i_Q_6) (bvult main_Q_0_Q_i_Q_6 (_ bv12 32))))) (=> ?v_13 (not (not (= (select main_Q_0_Q_b_Q_0 main_Q_0_Q_i_Q_6) (_ bv0 32))))))))))))))
(check-sat)
(exit)
