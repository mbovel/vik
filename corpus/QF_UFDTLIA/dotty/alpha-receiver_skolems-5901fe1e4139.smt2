; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `receiver_skolems.scala`
; original path: tests/*-custom-args/qualified-types/receiver_skolems.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Counter 0)
(declare-fun x0 () Int)
(declare-fun this_Counter () Counter)
(declare-fun Counter.max@Counter (Counter) Int)
(assert true)
(assert (not (<= x0 (Counter.max@Counter this_Counter))))
(check-sat)
