; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `eval_order.scala`
; original path: tests/*-custom-args/qualified-types/eval_order.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)8
; untyped: (param)9
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun sk_Test_6 () Int)
(declare-fun sk_Test_4 () Int)
(declare-fun x0 () Int)
(declare-fun sk_Test_9 () Int)
(declare-fun sk_Test_10 () Int)
(declare-fun sk_Test_2 () Int)
(assert (= sk_Test_6 sk_Test_4))
(assert (= x0 (+ sk_Test_9 sk_Test_10)))
(assert (not (= x0 (+ sk_Test_6 sk_Test_2))))
(check-sat)
