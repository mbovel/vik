; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices2.scala`
; original path: tests/*-custom-args/qualified-types/matrices2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)254
; untyped: (param)255
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((Matrix 0)) (((new_Matrix (Matrix.width@Matrix Int) (Matrix.height@Matrix Int)))))
(declare-fun x0 () Matrix)
(declare-fun sk_Test_568 () Int)
(declare-fun sk_Test_569 () Int)
(assert (<= 0 (Matrix.width@Matrix x0)))
(assert (<= 0 sk_Test_568))
(assert (<= 0 (Matrix.height@Matrix x0)))
(assert (<= 0 sk_Test_569))
(assert (<= 0 (Matrix.width@Matrix x0)))
(assert (<= 0 (Matrix.height@Matrix x0)))
(assert (and (= (Matrix.width@Matrix x0) sk_Test_568) (= (Matrix.height@Matrix x0) sk_Test_569)))
(assert (not (and (= (Matrix.width@Matrix x0) 2) (= (Matrix.height@Matrix x0) 3))))
(check-sat)
