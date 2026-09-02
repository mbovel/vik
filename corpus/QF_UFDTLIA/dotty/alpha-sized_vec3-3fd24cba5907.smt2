; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec3.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec3.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec[String]
; untyped: Vec[(String, Int)]
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun sk_example3_8 () Any)
(declare-fun sized_vec3$package$.len_String (Any) Int)
(declare-fun mPlusN () Int)
(declare-fun m () Int)
(declare-fun n () Int)
(declare-fun x0 () Any)
(declare-fun sized_vec3$package$.len__String__Int_ (Any) Int)
(assert (= (sized_vec3$package$.len_String sk_example3_8) mPlusN))
(assert (= mPlusN (+ m n)))
(assert (= (sized_vec3$package$.len_String sk_example3_8) (sized_vec3$package$.len__String__Int_ x0)))
(assert (not (= (+ m n) (sized_vec3$package$.len__String__Int_ x0))))
(check-sat)
