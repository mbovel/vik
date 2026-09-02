; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_unfolding_neg.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_unfolding_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: c1.T
; untyped: c2.T
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun x0 () Int)
(declare-fun subtyping_unfolding_neg$package$.tp_c1.T () Int)
(declare-fun subtyping_unfolding_neg$package$.tp_c2.T () Int)
(assert (= x0 subtyping_unfolding_neg$package$.tp_c1.T))
(assert (not (= x0 subtyping_unfolding_neg$package$.tp_c2.T)))
(check-sat)
