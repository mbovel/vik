; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mode_silent_flag.scala`
; original path: tests/*-custom-args/qualified-types/mode_silent_flag.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun mode_silent_flag$package$.getInt () Int)
(declare-fun x0 () Int)
(assert (= mode_silent_flag$package$.getInt x0))
(assert (not (= 1 x0)))
(check-sat)
