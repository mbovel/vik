; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `member_qualifiers_neg.scala`
; original path: tests/*-custom-args/qualified-types/member_qualifiers_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Params 0)
(declare-fun c1 () Params)
(declare-fun Params.a@Params (Params) Int)
(declare-fun Params.b@Params (Params) Int)
(declare-fun x0 () Int)
(assert (< (Params.a@Params c1) (Params.b@Params c1)))
(assert true)
(assert (not (< x0 (Params.b@Params c1))))
(check-sat)
