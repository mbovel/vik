; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip_2.scala`
; original path: tests/*-custom-args/qualified-types/list_zip_2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SLists.SList[A]
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_A 0)
(declare-sort Any 0)
(declare-fun sk_size_0 () Int)
(declare-fun x0 () Int)
(declare-fun a () Any)
(declare-fun SLists$.unwrap_A (Any) scala.collection.immutable.List_A)
(declare-fun SeqOps.size@scala.collection.immutable.List_A (scala.collection.immutable.List_A) Int)
(declare-fun Predef$.runtimeChecked_Int (Int) Int)
(assert (= sk_size_0 x0))
(assert (= sk_size_0 x0))
(assert (= x0 (Predef$.runtimeChecked_Int (SeqOps.size@scala.collection.immutable.List_A (SLists$.unwrap_A a)))))
(assert (not (<= 0 x0)))
(check-sat)
