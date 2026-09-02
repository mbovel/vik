; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip_2.scala`
; original path: tests/*-custom-args/qualified-types/list_zip_2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SLists.SList[A]
; untyped: A
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_A 0)
(declare-sort Any 0)
(declare-fun sk_cons_0 () scala.collection.immutable.List_A)
(declare-fun x0 () scala.collection.immutable.List_A)
(declare-fun list_zip_2$package$.size_A (scala.collection.immutable.List_A) Int)
(declare-fun xs () scala.collection.immutable.List_A)
(declare-fun SLists$.unwrap_A (Any) scala.collection.immutable.List_A)
(declare-fun x () Any)
(declare-fun List.__@scala.collection.immutable.List_A_A (scala.collection.immutable.List_A Any) scala.collection.immutable.List_A)
(declare-fun SLists$.wrap_A (scala.collection.immutable.List_A) scala.collection.immutable.List_A)
(declare-fun Predef$.runtimeChecked_SLists.SList_A_ (scala.collection.immutable.List_A) scala.collection.immutable.List_A)
(assert (= sk_cons_0 x0))
(assert (<= 0 (list_zip_2$package$.size_A x0)))
(assert (= sk_cons_0 x0))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (= x0 (Predef$.runtimeChecked_SLists.SList_A_ (SLists$.wrap_A (List.__@scala.collection.immutable.List_A_A (SLists$.unwrap_A xs) x)))))
(assert (not (= (list_zip_2$package$.size_A x0) (+ 1 (list_zip_2$package$.size_A xs)))))
(check-sat)
