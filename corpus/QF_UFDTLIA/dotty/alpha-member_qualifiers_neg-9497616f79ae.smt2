; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `member_qualifiers_neg.scala`
; original path: tests/*-custom-args/qualified-types/member_qualifiers_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Params 0)
(declare-sort Deep 0)
(declare-fun x0 () Int)
(declare-fun d () Deep)
(declare-fun Deep.c@Deep (Deep) Params)
(declare-fun Params.a@Params (Params) Int)
(assert true)
(assert (not (< x0 (Params.a@Params (Deep.c@Deep d)))))
(check-sat)
