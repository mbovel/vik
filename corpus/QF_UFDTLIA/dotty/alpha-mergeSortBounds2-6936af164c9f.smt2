; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds2.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: T
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.math.Ordering_T 0)
(declare-sort scala.collection.immutable.Seq_T 0)
(declare-sort Any 0)
(declare-fun ord () scala.math.Ordering_T)
(declare-fun left () scala.collection.immutable.Seq_T)
(declare-fun SafeSeqs$.head_T (scala.collection.immutable.Seq_T) Any)
(declare-fun right () scala.collection.immutable.Seq_T)
(declare-fun Ordering.lt@scala.math.Ordering_T (scala.math.Ordering_T Any Any) Bool)
(declare-fun SafeSeqs$.len_T (scala.collection.immutable.Seq_T) Int)
(declare-fun x0 () scala.collection.immutable.Seq_T)
(assert (Ordering.lt@scala.math.Ordering_T ord (SafeSeqs$.head_T left) (SafeSeqs$.head_T right)))
(assert (and (< 0 (SafeSeqs$.len_T left)) (< 0 (SafeSeqs$.len_T right))))
(assert true)
(assert (not (< 0 (SafeSeqs$.len_T x0))))
(check-sat)
