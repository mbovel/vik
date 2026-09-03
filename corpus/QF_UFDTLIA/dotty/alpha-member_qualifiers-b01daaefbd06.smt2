; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `member_qualifiers.scala`
; original path: tests/*-custom-args/qualified-types/member_qualifiers.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((ParamsCase 0)) (((new_ParamsCase (ParamsCase.a@ParamsCase Int) (ParamsCase.b@ParamsCase Int)))))
(declare-fun this_ParamsCase () ParamsCase)
(declare-fun x0 () Int)
(assert (< (ParamsCase.a@ParamsCase this_ParamsCase) (ParamsCase.b@ParamsCase this_ParamsCase)))
(assert true)
(assert (not (< x0 (ParamsCase.b@ParamsCase this_ParamsCase))))
(check-sat)
