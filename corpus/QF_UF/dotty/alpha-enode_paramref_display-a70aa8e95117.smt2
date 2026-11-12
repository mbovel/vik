; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `enode_paramref_display.scala`
; original path: tests/*-custom-args/qualified-types/enode_paramref_display.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UF, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UF)
(declare-sort scala.Function2_Int_Int_Boolean 0)
(declare-fun lambda () scala.Function2_Int_Int_Boolean)
(declare-fun toBool__Int__Int_____Boolean (scala.Function2_Int_Int_Boolean) Bool)
(declare-fun lambda~2 () scala.Function2_Int_Int_Boolean)
(assert (toBool__Int__Int_____Boolean lambda))
(assert (not (toBool__Int__Int_____Boolean lambda~2)))
(check-sat)
