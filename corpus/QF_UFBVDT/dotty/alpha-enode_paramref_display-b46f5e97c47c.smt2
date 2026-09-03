; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `enode_paramref_display.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/enode_paramref_display.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort scala.Function2_scala.Int_scala.Int_scala.Boolean 0)
(declare-fun lambda () scala.Function2_scala.Int_scala.Int_scala.Boolean)
(declare-fun toBool_scala.Function2_scala.Int_scala.Int_scala.Boolean (scala.Function2_scala.Int_scala.Int_scala.Boolean) Bool)
(assert true)
(assert (not (toBool_scala.Function2_scala.Int_scala.Int_scala.Boolean lambda)))
(check-sat)
