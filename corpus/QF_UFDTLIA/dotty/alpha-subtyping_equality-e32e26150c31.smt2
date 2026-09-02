; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_equality.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_equality.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((Box_scala.Int 0)) (((new_Box_scala.Int (Box.x@Box_scala.Int Int)))))
(declare-fun x0 () Box_scala.Int)
(assert (= x0 (new_Box_scala.Int 3)))
(assert (not (= x0 (new_Box_scala.Int 3))))
(check-sat)
