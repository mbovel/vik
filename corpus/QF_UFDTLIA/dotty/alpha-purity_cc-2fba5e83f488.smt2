; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `purity_cc.scala`
; original path: tests/*-custom-args/qualified-types/purity_cc.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Cell 0)
(declare-fun c () Cell)
(declare-fun Cell.field@Cell (Cell) Int)
(declare-fun x0 () Int)
(assert (= (Cell.field@Cell c) x0))
(assert (= (Cell.field@Cell c) x0))
(assert (= (Cell.field@Cell c) x0))
(assert (not (= (Cell.field@Cell c) x0)))
(check-sat)
