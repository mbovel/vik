; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `unfoldTypeApply.scala`
; original path: tests/*-custom-args/qualified-types/unfoldTypeApply.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_scala.Int 0)
(declare-sort scala.collection.immutable.Nil$ 0)
(declare-fun l () scala.collection.immutable.List_scala.Int)
(declare-fun unfoldTypeApply$package$.isNonEmpty_scala.Int (scala.collection.immutable.List_scala.Int) Bool)
(declare-fun package$.Nil () scala.collection.immutable.Nil$)
(declare-fun Any.__@scala.collection.immutable.List_scala.Int (scala.collection.immutable.List_scala.Int scala.collection.immutable.Nil$) Bool)
(declare-fun x0 () scala.collection.immutable.List_scala.Int)
(assert (unfoldTypeApply$package$.isNonEmpty_scala.Int l))
(assert (= (unfoldTypeApply$package$.isNonEmpty_scala.Int l) (Any.__@scala.collection.immutable.List_scala.Int l package$.Nil)))
(assert true)
(assert (not (Any.__@scala.collection.immutable.List_scala.Int x0 package$.Nil)))
(check-sat)
