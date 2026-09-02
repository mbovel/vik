; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices.scala`
; original path: tests/*-custom-args/qualified-types/matrices.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun c () Int)
(declare-fun x0 () Int)
(declare-fun Predef$.runtimeChecked_Int (Int) Int)
(assert (= c x0))
(assert (= c x0))
(assert (= x0 (Predef$.runtimeChecked_Int c)))
(assert (not (<= 0 x0)))
(check-sat)
