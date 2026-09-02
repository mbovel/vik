; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortLength.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortLength.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort SafeSeq_scala.Int 0)
(declare-fun x0 () SafeSeq_scala.Int)
(declare-fun SafeSeq.len@SafeSeq_scala.Int (SafeSeq_scala.Int) Int)
(declare-fun s () SafeSeq_scala.Int)
(declare-fun bound0 () SafeSeq_scala.Int)
(assert (<= 0 (SafeSeq.len@SafeSeq_scala.Int x0)))
(assert (<= 0 (SafeSeq.len@SafeSeq_scala.Int s)))
(assert (= (SafeSeq.len@SafeSeq_scala.Int s) 8))
(assert (<= 0 (SafeSeq.len@SafeSeq_scala.Int bound0)))
(assert (<= 0 (SafeSeq.len@SafeSeq_scala.Int x0)))
(assert (= (SafeSeq.len@SafeSeq_scala.Int x0) (SafeSeq.len@SafeSeq_scala.Int s)))
(assert (not (= (SafeSeq.len@SafeSeq_scala.Int x0) 8)))
(check-sat)
