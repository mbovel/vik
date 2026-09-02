; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SafeSeqs.SafeSeq[T]
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.Seq_T 0)
(declare-sort Any 0)
(declare-datatypes ((scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_ 0)) (((new_scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_ (Tuple2._1@scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_ Any) (Tuple2._2@scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_ Any)))))
(declare-fun x1 () scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_)
(declare-fun null () Any)
(declare-fun Object.ne@scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_ (scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_ Any) Bool)
(declare-fun x0 () scala.collection.immutable.Seq_T)
(declare-fun SafeSeqs$.len_T (scala.collection.immutable.Seq_T) Int)
(assert (Object.ne@scala.Tuple2_SafeSeqs.SafeSeq_T__SafeSeqs.SafeSeq_T_ x1 null))
(assert true)
(assert (not (< 0 (SafeSeqs$.len_T x0))))
(check-sat)
