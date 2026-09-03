; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `order_totality.scala`
; original path: tests/*-custom-args/qualified-types/order_totality.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun y () Int)
(declare-fun x0 () Int)
(declare-fun x () Int)
(assert (= y x0))
(assert (= y x0))
(assert (= y x0))
(assert (= y x0))
(assert (not (and (<= x0 x) (<= x0 y))))
(check-sat)
