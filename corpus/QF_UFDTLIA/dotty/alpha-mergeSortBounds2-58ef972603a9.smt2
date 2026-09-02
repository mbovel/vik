; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds2.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.Seq_T 0)
(declare-fun left () scala.collection.immutable.Seq_T)
(declare-fun x0 () scala.collection.immutable.Seq_T)
(declare-fun SafeSeqs$.len_T (scala.collection.immutable.Seq_T) Int)
(declare-fun right () scala.collection.immutable.Seq_T)
(assert (= left x0))
(assert (= left x0))
(assert (and (< 0 (SafeSeqs$.len_T left)) (< 0 (SafeSeqs$.len_T right))))
(assert (= left x0))
(assert (not (< 0 (SafeSeqs$.len_T x0))))
(check-sat)
