; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `adapt.scala`
; original path: tests/*-custom-args/qualified-types/adapt.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((Box_scala.Int 0)) (((new_Box_scala.Int (Box.x@Box_scala.Int Int)))))
(declare-fun x0 () Box_scala.Int)
(declare-fun x () Int)
(assert true)
(assert (not (= x0 (new_Box_scala.Int (+ 1 x)))))
(check-sat)
