; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `runtimeChecked_dependent.scala`
; original path: tests/*-custom-args/qualified-types/runtimeChecked_dependent.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun sk_Test_0 () Int)
(declare-fun x0 () Int)
(declare-fun p_x () Int)
(assert (< sk_Test_0 x0))
(assert (not (< p_x x0)))
(check-sat)
