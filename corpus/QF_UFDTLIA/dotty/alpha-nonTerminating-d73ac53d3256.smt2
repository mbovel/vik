; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `nonTerminating.scala`
; original path: tests/*-custom-args/qualified-types/nonTerminating.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)11
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.Nil$ 0)
(declare-sort scala.collection.immutable.List_A 0)
(declare-sort Any 0)
(declare-fun x0 () Bool)
(declare-fun sk_asNonEmpty_9 () Any)
(declare-fun package$.Nil () scala.collection.immutable.Nil$)
(declare-fun Any.__@Any (Any scala.collection.immutable.Nil$) Bool)
(declare-fun l () scala.collection.immutable.List_A)
(declare-fun Any.__@scala.collection.immutable.List_A (scala.collection.immutable.List_A scala.collection.immutable.Nil$) Bool)
(assert (and (= true x0) (Any.__@Any sk_asNonEmpty_9 package$.Nil)))
(assert (not (and (= true x0) (Any.__@scala.collection.immutable.List_A l package$.Nil))))
(check-sat)
