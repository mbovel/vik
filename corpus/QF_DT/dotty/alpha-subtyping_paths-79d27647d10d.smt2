; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_paths.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_paths.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_DT, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_DT)
(declare-fun subtyping_paths$package$.tp_OuterO.InnerO.D () Bool)
(assert subtyping_paths$package$.tp_OuterO.InnerO.D)
(assert (not subtyping_paths$package$.tp_OuterO.InnerO.D))
(check-sat)
