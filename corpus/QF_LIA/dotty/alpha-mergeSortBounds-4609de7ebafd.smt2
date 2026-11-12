; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)8
; logic: QF_LIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_LIA)
(declare-fun x0 () Int)
(declare-fun sk_mergeSort_2 () Int)
(assert (<= 0 x0))
(assert (<= 0 sk_mergeSort_2))
(assert (<= 0 x0))
(assert (< x0 sk_mergeSort_2))
(assert (not (<= 0 x0)))
(check-sat)
