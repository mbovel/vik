; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mergeSortBounds2.scala`
; original path: tests/*-custom-args/qualified-types/mergeSortBounds2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SafeSeqs.SafeSeq[T]
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun right () Any)
(declare-fun x0 () Any)
(declare-fun left () Any)
(declare-fun SafeSeqs$.len_T (Any) Int)
(assert (= right x0))
(assert (= right x0))
(assert (and (< 0 (SafeSeqs$.len_T left)) (< 0 (SafeSeqs$.len_T right))))
(assert (= right x0))
(assert (not (< 0 (SafeSeqs$.len_T x0))))
(check-sat)
