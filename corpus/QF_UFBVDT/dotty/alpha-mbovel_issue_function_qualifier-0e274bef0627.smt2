; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `mbovel_issue_function_qualifier.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/mbovel_issue_function_qualifier.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_scala.collection.immutable.List_scala.Int_scala.Boolean 0)
(declare-sort scala.collection.immutable.List_scala.Int 0)
(declare-fun IntListStructure$.valid () scala.Function1_scala.collection.immutable.List_scala.Int_scala.Boolean)
(declare-fun x0 () scala.collection.immutable.List_scala.Int)
(declare-fun Function1.apply@scala.Function1_scala.collection.immutable.List_scala.Int_scala.Boolean (scala.Function1_scala.collection.immutable.List_scala.Int_scala.Boolean scala.collection.immutable.List_scala.Int) Bool)
(assert true)
(assert (not (Function1.apply@scala.Function1_scala.collection.immutable.List_scala.Int_scala.Boolean IntListStructure$.valid x0)))
(check-sat)
