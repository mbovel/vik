; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `overloads2.scala`
; original path: tests/*-custom-args/qualified-types/overloads2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.List_Int 0)
(declare-fun x0 () scala.collection.immutable.List_Int)
(declare-fun overloads2$package$.isSorted_Int (scala.collection.immutable.List_Int) Bool)
(assert true)
(assert (not (overloads2$package$.isSorted_Int x0)))
(check-sat)
