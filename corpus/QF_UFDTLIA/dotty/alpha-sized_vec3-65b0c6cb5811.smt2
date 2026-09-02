; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec3.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec3.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)69
; untyped: Vec[String]
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun m () Int)
(declare-fun n () Int)
(declare-fun mPlusN () Int)
(declare-fun x0 () Any)
(declare-fun sized_vec3$package$.len_String (Any) Int)
(declare-fun sk_example3_72 () Int)
(assert (= (+ m n) mPlusN))
(assert (= (sized_vec3$package$.len_String x0) sk_example3_72))
(assert (not (= mPlusN (sized_vec3$package$.len_String x0))))
(check-sat)
