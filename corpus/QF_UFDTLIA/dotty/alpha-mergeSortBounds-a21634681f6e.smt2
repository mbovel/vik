; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)54
; untyped: SafeSeqs.SafeSeq[T]
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun x0 () Int)
(declare-fun sk_mergeSort_26 () Int)
(declare-fun list () Any)
(declare-fun SafeSeqs$.len_T (Any) Int)
(declare-fun len () Int)
(assert (<= 0 x0))
(assert (<= 0 sk_mergeSort_26))
(assert (<= 0 x0))
(assert (= (SafeSeqs$.len_T list) len))
(assert (<= 0 len))
(assert (< x0 sk_mergeSort_26))
(assert (not (< x0 len)))
(check-sat)
