; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `arrfold.scala`
; original path: tests/*-custom-args/qualified-types/arrfold.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)19
; untyped: (param)20
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun sk_$anonfun_18 () Int)
(declare-fun x0 () Int)
(declare-fun sk_$anonfun_19 () Int)
(assert (and (<= sk_$anonfun_18 x0) (<= sk_$anonfun_19 x0)))
(assert (not (<= 0 x0)))
(check-sat)
