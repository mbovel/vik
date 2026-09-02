; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_unfolding.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_unfolding.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-fun x () Int)
(declare-fun x2 () Int)
(declare-fun x0 () Int)
(declare-fun subtyping_unfolding$package$.id_scala.Int (Int) Int)
(assert (= x x2))
(assert (= x0 (subtyping_unfolding$package$.id_scala.Int x)))
(assert (not (= x0 (subtyping_unfolding$package$.id_scala.Int x2))))
(check-sat)
