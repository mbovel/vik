; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec3.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec3.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)51
; untyped: sized_vec3$package.Vec[scala.Int]
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec3$package$.len_scala.Int (Any) Int)
(declare-fun sk_example3_55 () Int)
(declare-fun n () Int)
(assert (= (sized_vec3$package$.len_scala.Int x0) sk_example3_55))
(assert (not (= (sized_vec3$package$.len_scala.Int x0) n)))
(check-sat)
