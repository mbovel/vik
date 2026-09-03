; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.collection.immutable.Seq_T 0)
(declare-fun sk_len_0 () Int)
(declare-fun x0 () Int)
(declare-fun a () scala.collection.immutable.Seq_T)
(declare-fun SeqOps.length@scala.collection.immutable.Seq_T (scala.collection.immutable.Seq_T) Int)
(declare-fun Predef$.runtimeChecked_Int (Int) Int)
(assert (= sk_len_0 x0))
(assert (= sk_len_0 x0))
(assert (= x0 (Predef$.runtimeChecked_Int (SeqOps.length@scala.collection.immutable.Seq_T a))))
(assert (not (<= 0 x0)))
(check-sat)
