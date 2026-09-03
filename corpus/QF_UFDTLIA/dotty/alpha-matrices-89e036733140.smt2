; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices.scala`
; original path: tests/*-custom-args/qualified-types/matrices.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_List_T_ 0)
(declare-fun x0 () scala.collection.immutable.List_List_T_)
(declare-fun matrices$package$.width_T (scala.collection.immutable.List_List_T_) Int)
(declare-fun m () scala.collection.immutable.List_List_T_)
(declare-fun matrices$package$.height_T (scala.collection.immutable.List_List_T_) Int)
(assert true)
(assert (not (and (= (matrices$package$.width_T x0) (matrices$package$.height_T m)) (= (matrices$package$.height_T x0) (matrices$package$.width_T m)))))
(check-sat)
