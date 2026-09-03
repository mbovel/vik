; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `bijection.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/bijection.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: B
; untyped: A
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort Bijection_A_B 0)
(declare-sort Any 0)
(declare-fun this_Bijection () Bijection_A_B)
(declare-fun p_b () Any)
(declare-fun Bijection.g@Bijection_A_B (Bijection_A_B Any) Any)
(declare-fun Bijection.f@Bijection_A_B (Bijection_A_B Any) Any)
(declare-fun Any.__@Any (Any Any) Bool)
(assert true)
(assert (not (Any.__@Any (Bijection.f@Bijection_A_B this_Bijection (Bijection.g@Bijection_A_B this_Bijection p_b)) p_b)))
(check-sat)
