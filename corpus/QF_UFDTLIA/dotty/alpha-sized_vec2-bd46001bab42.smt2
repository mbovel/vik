; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec2.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: sized_vec2$package.Vec[scala.Int]
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec2$package$.len_scala.Int (Any) Int)
(declare-fun m () Int)
(assert (<= 0 (sized_vec2$package$.len_scala.Int x0)))
(assert (<= 0 m))
(assert (<= 0 (sized_vec2$package$.len_scala.Int x0)))
(assert (<= 0 m))
(assert (= (sized_vec2$package$.len_scala.Int x0) m))
(assert (not (= (sized_vec2$package$.len_scala.Int x0) m)))
(check-sat)
