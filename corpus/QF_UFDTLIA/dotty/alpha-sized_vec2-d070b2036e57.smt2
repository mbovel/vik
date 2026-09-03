; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec2.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec[String]
; untyped: (param)29
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec2$package$.len_String (Any) Int)
(declare-fun m () Int)
(declare-fun n () Int)
(declare-fun mPlusN () Int)
(declare-fun sk_Test_28 () Int)
(assert (<= 0 (sized_vec2$package$.len_String x0)))
(assert (<= 0 (sized_vec2$package$.len_String x0)))
(assert (= (+ m n) mPlusN))
(assert (<= 0 m))
(assert (<= 0 n))
(assert (= (sized_vec2$package$.len_String x0) sk_Test_28))
(assert (not (= (sized_vec2$package$.len_String x0) mPlusN)))
(check-sat)
