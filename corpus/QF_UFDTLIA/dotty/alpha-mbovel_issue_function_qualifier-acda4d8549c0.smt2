; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mbovel_issue_function_qualifier.scala`
; original path: tests/*-custom-args/qualified-types/mbovel_issue_function_qualifier.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_IntListStructure.S_Boolean 0)
(declare-sort scala.collection.immutable.List_Int 0)
(declare-fun IntListStructure$.valid () scala.Function1_IntListStructure.S_Boolean)
(declare-fun x0 () scala.collection.immutable.List_Int)
(declare-fun Function1.apply@scala.Function1_IntListStructure.S_Boolean (scala.Function1_IntListStructure.S_Boolean scala.collection.immutable.List_Int) Bool)
(assert true)
(assert (not (Function1.apply@scala.Function1_IntListStructure.S_Boolean IntListStructure$.valid x0)))
(check-sat)
