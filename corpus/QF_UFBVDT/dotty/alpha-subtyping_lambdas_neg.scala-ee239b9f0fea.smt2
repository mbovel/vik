; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_lambdas_neg.scala.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_lambdas_neg.scala.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort scala.Function2_java.lang.String_java.lang.String_scala.Any 0)
(declare-fun lambda () scala.Function2_java.lang.String_java.lang.String_scala.Any)
(declare-fun subtyping_lambdas_neg.scala$package$.toBool_scala.Function2_java.lang.String_java.lang.String_scala.Any (scala.Function2_java.lang.String_java.lang.String_scala.Any) Bool)
(declare-fun lambda~2 () scala.Function2_java.lang.String_java.lang.String_scala.Any)
(assert (subtyping_lambdas_neg.scala$package$.toBool_scala.Function2_java.lang.String_java.lang.String_scala.Any lambda))
(assert (not (subtyping_lambdas_neg.scala$package$.toBool_scala.Function2_java.lang.String_java.lang.String_scala.Any lambda~2)))
(check-sat)
