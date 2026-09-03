; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip_2.scala`
; original path: tests/*-custom-args/qualified-types/list_zip_2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SLists.SList[A]
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List__A__B_ 0)
(declare-sort Any 0)
(declare-fun x0 () scala.collection.immutable.List__A__B_)
(declare-fun list_zip_2$package$.size__A__B_ (scala.collection.immutable.List__A__B_) Int)
(declare-fun xs () Any)
(declare-fun list_zip_2$package$.size_A (Any) Int)
(assert (<= 0 (list_zip_2$package$.size__A__B_ x0)))
(assert (<= 0 (list_zip_2$package$.size__A__B_ x0)))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (= 0 (list_zip_2$package$.size__A__B_ x0)))
(assert (not (= (list_zip_2$package$.size__A__B_ x0) (list_zip_2$package$.size_A xs))))
(check-sat)
