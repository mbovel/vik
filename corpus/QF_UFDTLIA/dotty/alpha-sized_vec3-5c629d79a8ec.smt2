; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec3.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec3.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)53
; untyped: sized_vec3$package.Vec[scala.Int]
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec3$package$.len_scala.Int (Any) Int)
(declare-fun sk_example3_56 () Int)
(declare-fun m () Int)
(assert (= (sized_vec3$package$.len_scala.Int x0) sk_example3_56))
(assert (not (= (sized_vec3$package$.len_scala.Int x0) m)))
(check-sat)
