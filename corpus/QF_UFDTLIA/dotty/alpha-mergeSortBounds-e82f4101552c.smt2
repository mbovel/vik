; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)66
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.Seq_T 0)
(declare-fun x0 () Int)
(declare-fun sk_mergeSort_34 () Int)
(declare-fun list () scala.collection.immutable.Seq_T)
(declare-fun SafeSeqs$.len_T (scala.collection.immutable.Seq_T) Int)
(declare-fun len () Int)
(assert (<= 0 x0))
(assert (<= 0 sk_mergeSort_34))
(assert (<= 0 x0))
(assert (= (SafeSeqs$.len_T list) len))
(assert (<= 0 len))
(assert (< x0 sk_mergeSort_34))
(assert (not (< x0 len)))
(check-sat)
