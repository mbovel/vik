; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `scala-workshop-25.scala`
; original path: tests/*-custom-args/qualified-types/scala-workshop-25.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_Int 0)
(declare-fun sk_main_3 () scala.collection.immutable.List_Int)
(declare-fun SeqOps.size@scala.collection.immutable.List_Int (scala.collection.immutable.List_Int) Int)
(declare-fun xs () scala.collection.immutable.List_Int)
(declare-fun ys () scala.collection.immutable.List_Int)
(declare-fun x0 () scala.collection.immutable.List_Int)
(assert (= (SeqOps.size@scala.collection.immutable.List_Int sk_main_3) (+ (SeqOps.size@scala.collection.immutable.List_Int xs) (SeqOps.size@scala.collection.immutable.List_Int ys))))
(assert (= (+ (SeqOps.size@scala.collection.immutable.List_Int xs) (SeqOps.size@scala.collection.immutable.List_Int ys)) (SeqOps.size@scala.collection.immutable.List_Int x0)))
(assert (not (= (SeqOps.size@scala.collection.immutable.List_Int sk_main_3) (SeqOps.size@scala.collection.immutable.List_Int x0))))
(check-sat)
