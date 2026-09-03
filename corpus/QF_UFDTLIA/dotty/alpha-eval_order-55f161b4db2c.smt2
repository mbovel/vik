; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `eval_order.scala`
; original path: tests/*-custom-args/qualified-types/eval_order.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)66
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort Str 0)
(declare-fun str () Str)
(declare-fun eval_order$package$.eff (Str Int) Int)
(declare-fun x$2 () Int)
(declare-fun x0 () Int)
(declare-fun sk_Test_99 () Int)
(assert (= (eval_order$package$.eff str 2) x$2))
(assert (= x0 sk_Test_99))
(assert (not (= x$2 x0)))
(check-sat)
