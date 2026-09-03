; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `receiver_skolems.scala`
; original path: tests/*-custom-args/qualified-types/receiver_skolems.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)78
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort Counter 0)
(declare-fun x0 () Counter)
(declare-fun Counter.max@Counter (Counter) Int)
(declare-fun sk_tests_76 () Int)
(assert (= (Counter.max@Counter x0) sk_tests_76))
(assert (not (= (Counter.max@Counter x0) 10)))
(check-sat)
