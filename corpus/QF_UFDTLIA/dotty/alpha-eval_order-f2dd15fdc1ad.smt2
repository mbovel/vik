; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `eval_order.scala`
; original path: tests/*-custom-args/qualified-types/eval_order.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)20
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun x0 () Int)
(declare-fun sk_Test_26 () Int)
(declare-fun sk_Test_4 () Int)
(assert (= x0 sk_Test_26))
(assert (not (= x0 sk_Test_4)))
(check-sat)
