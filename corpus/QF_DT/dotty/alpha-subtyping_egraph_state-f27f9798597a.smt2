; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_egraph_state.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_egraph_state.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_DT, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_DT)
(declare-fun b () Bool)
(declare-fun b2 () Bool)
(assert (and b b2))
(assert (not b))
(check-sat)
