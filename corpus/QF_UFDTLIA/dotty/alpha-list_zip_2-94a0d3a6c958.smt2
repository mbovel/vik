; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip_2.scala`
; original path: tests/*-custom-args/qualified-types/list_zip_2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SLists.SList[A]
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_A 0)
(declare-fun x0 () scala.collection.immutable.List_A)
(declare-fun list_zip_2$package$.size_A (scala.collection.immutable.List_A) Int)
(declare-fun xs () scala.collection.immutable.List_A)
(assert (<= 0 (list_zip_2$package$.size_A x0)))
(assert (< 0 (list_zip_2$package$.size_A xs)))
(assert (not (= 0 (list_zip_2$package$.size_A xs))))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert true)
(assert (not (< 0 (list_zip_2$package$.size_A x0))))
(check-sat)
