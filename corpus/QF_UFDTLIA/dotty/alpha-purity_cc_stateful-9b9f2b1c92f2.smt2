; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `purity_cc_stateful.scala`
; original path: tests/*-custom-args/qualified-types/purity_cc_stateful.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Cell 0)
(declare-fun c () Cell)
(declare-fun Cell.field@Cell (Cell) Int)
(declare-fun snapshot () Int)
(declare-fun purity_cc_stateful$package$.pure (Int) Int)
(declare-fun x0 () Int)
(assert (= (Cell.field@Cell c) snapshot))
(assert (= (Cell.field@Cell c) snapshot))
(assert (= (purity_cc_stateful$package$.pure snapshot) x0))
(assert (not (= (purity_cc_stateful$package$.pure snapshot) x0)))
(check-sat)
