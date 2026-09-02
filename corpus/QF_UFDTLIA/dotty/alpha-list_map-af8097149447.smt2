; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_map.scala`
; original path: tests/*-custom-args/qualified-types/list_map.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun x () Int)
(declare-fun sk_inc_0 () Int)
(declare-fun x0 () Int)
(declare-fun Predef$.runtimeChecked_scala.Int (Int) Int)
(assert (< 0 x))
(assert (= sk_inc_0 x0))
(assert (= sk_inc_0 x0))
(assert (= x0 (Predef$.runtimeChecked_scala.Int (+ 1 x))))
(assert (not (< 0 x0)))
(check-sat)
