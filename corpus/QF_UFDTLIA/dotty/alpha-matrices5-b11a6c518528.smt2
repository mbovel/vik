; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices5.scala`
; original path: tests/*-custom-args/qualified-types/matrices5.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun from () Int)
(declare-fun to () Int)
(declare-fun bound0 () Int)
(declare-fun x0 () Int)
(assert (< from to))
(assert (<= 0 from))
(assert (<= from to))
(assert (<= 0 bound0))
(assert (<= 0 from))
(assert (<= 0 to))
(assert true)
(assert (not (<= 0 x0)))
(check-sat)
