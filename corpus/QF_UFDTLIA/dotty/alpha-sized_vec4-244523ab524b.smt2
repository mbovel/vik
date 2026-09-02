; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec4.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec4.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)5
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Vec_scala.Int 0)
(declare-sort Any 0)
(declare-fun v1 () Vec_scala.Int)
(declare-fun Vec.len@Vec_scala.Int (Vec_scala.Int) Int)
(declare-fun n () Int)
(declare-fun v2 () Vec_scala.Int)
(declare-fun m () Int)
(declare-fun x0 () Vec_scala.Int)
(declare-fun sk_example3_5 () Any)
(declare-fun Vec.len@Any (Any) Int)
(assert (= (Vec.len@Vec_scala.Int v1) n))
(assert (= (Vec.len@Vec_scala.Int v1) n))
(assert (= (Vec.len@Vec_scala.Int v2) m))
(assert (= (Vec.len@Vec_scala.Int x0) (+ (Vec.len@Vec_scala.Int v1) (Vec.len@Any sk_example3_5))))
(assert (not (= (Vec.len@Vec_scala.Int x0) (+ (Vec.len@Vec_scala.Int v1) (Vec.len@Vec_scala.Int v2)))))
(check-sat)
