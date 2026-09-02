; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: T
; untyped: SafeSeqs.SafeSeq[T]
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.math.Ordering_T 0)
(declare-sort scala.collection.immutable.Seq_T 0)
(declare-sort Any 0)
(declare-fun ord () scala.math.Ordering_T)
(declare-fun left () Any)
(declare-fun SafeSeqs$.head_T (Any) Any)
(declare-fun right () Any)
(declare-fun Ordering.lt@scala.math.Ordering_T (scala.math.Ordering_T Any Any) Bool)
(declare-fun SafeSeqs$.len_T (scala.collection.immutable.Seq_T) Int)
(declare-fun x0 () scala.collection.immutable.Seq_T)
(assert (not (Ordering.lt@scala.math.Ordering_T ord (SafeSeqs$.head_T left) (SafeSeqs$.head_T right))))
(assert (< 0 (SafeSeqs$.len_T left)))
(assert (< 0 (SafeSeqs$.len_T right)))
(assert (< 0 (SafeSeqs$.len_T x0)))
(assert (not (< 0 (SafeSeqs$.len_T x0))))
(check-sat)
