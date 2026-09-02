; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_objects.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_objects.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Box_scala.Int 0)
(declare-fun x0 () Box_scala.Int)
(declare-fun Box.x@Box_scala.Int (Box_scala.Int) Int)
(assert (= 3 (Box.x@Box_scala.Int x0)))
(assert (not (= 3 (Box.x@Box_scala.Int x0))))
(check-sat)
