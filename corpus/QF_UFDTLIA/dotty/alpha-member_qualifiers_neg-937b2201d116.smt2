; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `member_qualifiers_neg.scala`
; original path: tests/*-custom-args/qualified-types/member_qualifiers_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort Params 0)
(declare-fun c2 () Params)
(declare-fun Params.a@Params (Params) Int)
(declare-fun x0 () Int)
(declare-fun c1 () Params)
(declare-fun Params.b@Params (Params) Int)
(assert (= (Params.a@Params c2) x0))
(assert (= (Params.a@Params c2) x0))
(assert (< (Params.a@Params c1) (Params.b@Params c1)))
(assert (= (Params.a@Params c2) x0))
(assert (not (< x0 (Params.b@Params c1))))
(check-sat)
