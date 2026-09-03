; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `scala-workshop-25.scala`
; original path: tests/*-custom-args/qualified-types/scala-workshop-25.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)5
; untyped: (param)6
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_Int 0)
(declare-sort scala.collection.immutable.List_T 0)
(declare-sort Any 0)
(declare-fun sk_main_3 () scala.collection.immutable.List_Int)
(declare-fun SeqOps.size@scala.collection.immutable.List_Int (scala.collection.immutable.List_Int) Int)
(declare-fun xs () scala.collection.immutable.List_Int)
(declare-fun ys () scala.collection.immutable.List_Int)
(declare-fun x0 () scala.collection.immutable.List_T)
(declare-fun SeqOps.size@scala.collection.immutable.List_T (scala.collection.immutable.List_T) Int)
(declare-fun sk_main_5 () Any)
(declare-fun SeqOps.size@Any (Any) Int)
(declare-fun sk_main_6 () Any)
(assert (= (SeqOps.size@scala.collection.immutable.List_Int sk_main_3) (+ (SeqOps.size@scala.collection.immutable.List_Int xs) (SeqOps.size@scala.collection.immutable.List_Int ys))))
(assert (= (SeqOps.size@scala.collection.immutable.List_T x0) (+ (SeqOps.size@Any sk_main_5) (SeqOps.size@Any sk_main_6))))
(assert (not (= (SeqOps.size@scala.collection.immutable.List_Int sk_main_3) (SeqOps.size@scala.collection.immutable.List_T x0))))
(check-sat)
