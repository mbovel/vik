; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip_2.scala`
; original path: tests/*-custom-args/qualified-types/list_zip_2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)119
; untyped: SLists.SList[A]
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_A 0)
(declare-fun x0 () scala.collection.immutable.List_A)
(declare-fun list_zip_2$package$.size_A (scala.collection.immutable.List_A) Int)
(declare-fun sk_zip_106 () scala.collection.immutable.List_A)
(declare-fun bound0 () scala.collection.immutable.List_A)
(declare-fun xs () scala.collection.immutable.List_A)
(assert (<= 0 (list_zip_2$package$.size_A x0)))
(assert (<= 0 (list_zip_2$package$.size_A sk_zip_106)))
(assert (< 0 (list_zip_2$package$.size_A sk_zip_106)))
(assert (<= 0 (list_zip_2$package$.size_A bound0)))
(assert (<= 0 (list_zip_2$package$.size_A x0)))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (< 0 (list_zip_2$package$.size_A xs)))
(assert (not (= 0 (list_zip_2$package$.size_A xs))))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (= (list_zip_2$package$.size_A x0) (+ (- 1) (list_zip_2$package$.size_A sk_zip_106))))
(assert (not (= (list_zip_2$package$.size_A x0) (+ (- 1) (list_zip_2$package$.size_A xs)))))
(check-sat)
