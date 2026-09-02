; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices2.scala`
; original path: tests/*-custom-args/qualified-types/matrices2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)191
; untyped: (param)192
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-datatypes ((Matrix 0)) (((new_Matrix (Matrix.width@Matrix Int) (Matrix.height@Matrix Int)))))
(declare-fun x0 () Matrix)
(declare-fun sk_mul_29 () Int)
(declare-fun sk_mul_30 () Int)
(declare-fun that () Matrix)
(declare-fun this_Matrix () Matrix)
(declare-fun bound0 () Matrix)
(assert (<= 0 (Matrix.width@Matrix x0)))
(assert (<= 0 sk_mul_29))
(assert (<= 0 (Matrix.height@Matrix x0)))
(assert (<= 0 sk_mul_30))
(assert (<= 0 (Matrix.width@Matrix x0)))
(assert (<= 0 (Matrix.width@Matrix that)))
(assert (= (Matrix.height@Matrix that) (Matrix.width@Matrix this_Matrix)))
(assert (<= 0 (Matrix.height@Matrix bound0)))
(assert (<= 0 (Matrix.width@Matrix this_Matrix)))
(assert (<= 0 (Matrix.height@Matrix x0)))
(assert (<= 0 (Matrix.height@Matrix this_Matrix)))
(assert (and (= (Matrix.width@Matrix x0) sk_mul_29) (= (Matrix.height@Matrix x0) sk_mul_30)))
(assert (not (and (= (Matrix.width@Matrix x0) (Matrix.width@Matrix that)) (= (Matrix.height@Matrix x0) (Matrix.height@Matrix this_Matrix)))))
(check-sat)
