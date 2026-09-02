; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortLength.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortLength.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)241
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-fun x0 () Int)
(declare-fun sk_mergeSort_407 () Int)
(assert (<= 0 x0))
(assert (<= 0 sk_mergeSort_407))
(assert (<= 0 x0))
(assert (< x0 sk_mergeSort_407))
(assert (not (<= 0 x0)))
(check-sat)
