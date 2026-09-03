; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_unfolding_neg.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_unfolding_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort C 0)
(declare-fun x0 () C)
(declare-fun c1 () C)
(declare-fun Any.__@C (C C) Bool)
(declare-fun c2 () C)
(assert (Any.__@C x0 c1))
(assert (not (Any.__@C x0 c2)))
(check-sat)
