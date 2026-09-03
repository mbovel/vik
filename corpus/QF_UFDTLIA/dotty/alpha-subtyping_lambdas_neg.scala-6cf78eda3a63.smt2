; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_lambdas_neg.scala.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_lambdas_neg.scala.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_Int_Int____Boolean 0)
(declare-fun lambda () scala.Function1_Int_Int____Boolean)
(declare-fun subtyping_lambdas_neg.scala$package$.toBool_Int____Int____Boolean (scala.Function1_Int_Int____Boolean) Bool)
(declare-fun lambda~2 () scala.Function1_Int_Int____Boolean)
(assert (subtyping_lambdas_neg.scala$package$.toBool_Int____Int____Boolean lambda))
(assert (not (subtyping_lambdas_neg.scala$package$.toBool_Int____Int____Boolean lambda~2)))
(check-sat)
