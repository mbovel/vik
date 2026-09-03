; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `collect.scala`
; original path: tests/*-custom-args/qualified-types/collect.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: T
; untyped: (param)77
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_S_Boolean 0)
(declare-sort Any 0)
(declare-fun p () scala.Function1_S_Boolean)
(declare-fun x () Any)
(declare-fun Function1.apply@scala.Function1_S_Boolean (scala.Function1_S_Boolean Any) Bool)
(declare-fun x0 () Any)
(declare-fun sk_collect_26 () Any)
(declare-fun Function1.apply@Any (Any Any) Bool)
(assert (not (Function1.apply@scala.Function1_S_Boolean p x)))
(assert (Function1.apply@scala.Function1_S_Boolean p x0))
(assert (not (Function1.apply@Any sk_collect_26 x0)))
(check-sat)
