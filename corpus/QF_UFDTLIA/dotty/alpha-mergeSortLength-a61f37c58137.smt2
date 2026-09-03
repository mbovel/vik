; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortLength.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortLength.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)74
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort SafeSeq_T 0)
(declare-fun x0 () Int)
(declare-fun sk_mergeSort_112 () Int)
(declare-fun list () SafeSeq_T)
(declare-fun SafeSeq.len@SafeSeq_T (SafeSeq_T) Int)
(declare-fun len () Int)
(assert (<= 0 x0))
(assert (<= 0 sk_mergeSort_112))
(assert (<= 0 x0))
(assert (= (SafeSeq.len@SafeSeq_T list) len))
(assert (<= 0 (SafeSeq.len@SafeSeq_T list)))
(assert (<= 0 len))
(assert (< x0 sk_mergeSort_112))
(assert (not (< x0 len)))
(check-sat)
