; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `trait.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/trait.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-datatypes ((CT 0)) (((new_CT))))
(declare-fun this_CT () CT)
(declare-fun CT.valid@CT (CT) Bool)
(assert (CT.valid@CT this_CT))
(assert (not (CT.valid@CT this_CT)))
(check-sat)
