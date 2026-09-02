; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip_2.scala`
; original path: tests/*-custom-args/qualified-types/list_zip_2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)30
; untyped: SLists.SList[B]
; untyped: SLists.SList[A]
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_B 0)
(declare-sort Any 0)
(declare-fun x0 () scala.collection.immutable.List_B)
(declare-fun list_zip_2$package$.size_B (scala.collection.immutable.List_B) Int)
(declare-fun sk_zip_22 () scala.collection.immutable.List_B)
(declare-fun bound0 () scala.collection.immutable.List_B)
(declare-fun ys () scala.collection.immutable.List_B)
(declare-fun xs () Any)
(declare-fun list_zip_2$package$.size_A (Any) Int)
(assert (<= 0 (list_zip_2$package$.size_B x0)))
(assert (<= 0 (list_zip_2$package$.size_B sk_zip_22)))
(assert (< 0 (list_zip_2$package$.size_B sk_zip_22)))
(assert (<= 0 (list_zip_2$package$.size_B bound0)))
(assert (<= 0 (list_zip_2$package$.size_B x0)))
(assert (<= 0 (list_zip_2$package$.size_B ys)))
(assert (= (list_zip_2$package$.size_B ys) (list_zip_2$package$.size_A xs)))
(assert (<= 0 (list_zip_2$package$.size_B bound0)))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (< 0 (list_zip_2$package$.size_A xs)))
(assert (not (= 0 (list_zip_2$package$.size_A xs))))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (= (list_zip_2$package$.size_B x0) (+ (- 1) (list_zip_2$package$.size_B sk_zip_22))))
(assert (not (= (list_zip_2$package$.size_B x0) (+ (- 1) (list_zip_2$package$.size_B ys)))))
(check-sat)
