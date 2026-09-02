; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices4.scala`
; original path: tests/*-custom-args/qualified-types/matrices4.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-datatypes ((Matrix 0)) (((new_Matrix (Matrix.width@Matrix Int) (Matrix.height@Matrix Int)))))
(declare-fun x0 () Matrix)
(declare-fun m1 () Matrix)
(declare-fun w () Int)
(declare-fun h () Int)
(assert (<= 0 (Matrix.height@Matrix x0)))
(assert (<= 0 (Matrix.width@Matrix m1)))
(assert (= m1 (new_Matrix w h)))
(assert (<= 0 w))
(assert (<= 0 h))
(assert true)
(assert (not (= (Matrix.height@Matrix x0) (Matrix.width@Matrix m1))))
(check-sat)
