; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `return_type_assumptions.scala`
; original path: tests/*-custom-args/qualified-types/return_type_assumptions.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)19
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun x0 () Int)
(declare-fun sk_return_type_assumptions$package$_29 () Int)
(assert (= x0 (+ 1 sk_return_type_assumptions$package$_29)))
(assert (not (= x0 5)))
(check-sat)
