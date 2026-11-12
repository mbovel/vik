; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `trait.scala`
; original path: tests/*-custom-args/qualified-types/trait.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFDT, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDT)
(declare-datatypes ((CT 0)) (((new_CT))))
(declare-fun this_CT () CT)
(declare-fun CT.valid@CT (CT) Bool)
(assert (CT.valid@CT this_CT))
(assert (not (CT.valid@CT this_CT)))
(check-sat)
