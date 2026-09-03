; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `avoidance.scala`
; original path: tests/*-custom-args/qualified-types/avoidance.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)108
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun p_x () Int)
(declare-fun sk_test4_44 () Int)
(declare-fun x$1 () Int)
(declare-fun y$1 () Int)
(declare-fun getInt () Int)
(declare-fun x0 () Int)
(assert (< p_x sk_test4_44))
(assert (< x$1 y$1))
(assert (= x$1 getInt))
(assert (= sk_test4_44 x0))
(assert (not (= y$1 x0)))
(check-sat)
