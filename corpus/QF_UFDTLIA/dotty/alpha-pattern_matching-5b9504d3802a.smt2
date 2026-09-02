; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `pattern_matching.scala`
; original path: tests/*-custom-args/qualified-types/pattern_matching.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Str 0)
(declare-fun x0 () Str)
(declare-fun String.isEmpty@Str (Str) Bool)
(assert true)
(assert (not (not (String.isEmpty@Str x0))))
(check-sat)
