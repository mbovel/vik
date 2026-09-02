; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `branch_conditions.scala`
; original path: tests/*-custom-args/qualified-types/branch_conditions.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-fun x () Int)
(declare-fun x0 () Int)
(declare-fun y () Int)
(assert (= x x0))
(assert (= x x0))
(assert (not (= x y)))
(assert (= x x0))
(assert (not (= x x0)))
(check-sat)
