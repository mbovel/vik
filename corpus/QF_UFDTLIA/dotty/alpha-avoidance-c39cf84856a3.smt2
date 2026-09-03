; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `avoidance.scala`
; original path: tests/*-custom-args/qualified-types/avoidance.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)53
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun p_x () Int)
(declare-fun sk_test4_19 () Int)
(declare-fun sk_test4_1 () Int)
(declare-fun sk_test4_3 () Int)
(declare-fun x0 () Int)
(assert (< p_x sk_test4_19))
(assert (< sk_test4_1 sk_test4_3))
(assert (= sk_test4_19 x0))
(assert (not (= sk_test4_3 x0)))
(check-sat)
