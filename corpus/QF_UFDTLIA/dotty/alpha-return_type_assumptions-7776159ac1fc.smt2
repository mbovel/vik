; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `return_type_assumptions.scala`
; original path: tests/*-custom-args/qualified-types/return_type_assumptions.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-fun sk_w_5 () Int)
(declare-fun x0 () Int)
(assert (= sk_w_5 4))
(assert (= x0 (+ 1 sk_w_5)))
(assert (not (= x0 5)))
(check-sat)
