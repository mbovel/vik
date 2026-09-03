; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices3.scala`
; original path: tests/*-custom-args/qualified-types/matrices3.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((Matrix 0)) (((new_Matrix (Matrix.width@Matrix Int) (Matrix.height@Matrix Int)))))
(declare-fun this_Matrix () Matrix)
(declare-fun Matrix.size@Matrix (Matrix) Int)
(declare-fun x0 () Int)
(declare-fun Predef$.runtimeChecked_Int (Int) Int)
(assert (= (Matrix.size@Matrix this_Matrix) x0))
(assert (= (Matrix.size@Matrix this_Matrix) x0))
(assert (= x0 (Predef$.runtimeChecked_Int (Matrix.size@Matrix this_Matrix))))
(assert (not (<= 0 x0)))
(check-sat)
