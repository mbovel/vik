; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_lambdas_neg.scala.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_lambdas_neg.scala.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_scala.Double_scala.Function1_scala.Int_scala.Boolean 0)
(declare-fun lambda () scala.Function1_scala.Double_scala.Function1_scala.Int_scala.Boolean)
(declare-fun subtyping_lambdas_neg.scala$package$.toBool_scala.Function1_scala.Double_scala.Function1_scala.Int_scala.Boolean (scala.Function1_scala.Double_scala.Function1_scala.Int_scala.Boolean) Bool)
(declare-fun lambda~2 () scala.Function1_scala.Double_scala.Function1_scala.Int_scala.Boolean)
(assert (subtyping_lambdas_neg.scala$package$.toBool_scala.Function1_scala.Double_scala.Function1_scala.Int_scala.Boolean lambda))
(assert (not (subtyping_lambdas_neg.scala$package$.toBool_scala.Function1_scala.Double_scala.Function1_scala.Int_scala.Boolean lambda~2)))
(check-sat)
