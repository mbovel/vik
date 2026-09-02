; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec4.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec4.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Vec_scala.Predef.String 0)
(declare-sort Vec_scala.Int 0)
(declare-fun v3 () Vec_scala.Predef.String)
(declare-fun Vec.len@Vec_scala.Predef.String (Vec_scala.Predef.String) Int)
(declare-fun n () Int)
(declare-fun m () Int)
(declare-fun x0 () Vec_scala.Int)
(declare-fun Vec.len@Vec_scala.Int (Vec_scala.Int) Int)
(assert (= (Vec.len@Vec_scala.Predef.String v3) (+ n m)))
(assert (= (Vec.len@Vec_scala.Predef.String v3) (+ n m)))
(assert (= (Vec.len@Vec_scala.Predef.String v3) (Vec.len@Vec_scala.Int x0)))
(assert (not (= (Vec.len@Vec_scala.Predef.String v3) (Vec.len@Vec_scala.Int x0))))
(check-sat)
