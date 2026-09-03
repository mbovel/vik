; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices2.scala`
; original path: tests/*-custom-args/qualified-types/matrices2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)285
; untyped: (param)286
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((Matrix 0)) (((new_Matrix (Matrix.width@Matrix Int) (Matrix.height@Matrix Int)))))
(declare-fun x0 () Matrix)
(declare-fun sk_transpose_45 () Int)
(declare-fun sk_transpose_46 () Int)
(declare-fun this_Matrix () Matrix)
(assert (<= 0 (Matrix.width@Matrix x0)))
(assert (<= 0 sk_transpose_45))
(assert (<= 0 (Matrix.height@Matrix x0)))
(assert (<= 0 sk_transpose_46))
(assert (<= 0 (Matrix.width@Matrix x0)))
(assert (<= 0 (Matrix.height@Matrix this_Matrix)))
(assert (<= 0 (Matrix.height@Matrix x0)))
(assert (<= 0 (Matrix.width@Matrix this_Matrix)))
(assert (and (= (Matrix.width@Matrix x0) sk_transpose_45) (= (Matrix.height@Matrix x0) sk_transpose_46)))
(assert (not (and (= (Matrix.width@Matrix x0) (Matrix.height@Matrix this_Matrix)) (= (Matrix.height@Matrix x0) (Matrix.width@Matrix this_Matrix)))))
(check-sat)
