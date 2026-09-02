; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `adapt.scala`
; original path: tests/*-custom-args/qualified-types/adapt.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((IntBox 0)) (((new_IntBox (IntBox.x@IntBox Int)))))
(declare-fun x () Int)
(declare-fun x0 () IntBox)
(assert (= (new_IntBox (+ 1 x)) x0))
(assert (not (= (new_IntBox (+ 1 x)) x0)))
(check-sat)
