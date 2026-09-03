; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `bijection.scala`
; original path: tests/*-custom-args/qualified-types/bijection.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: B
; untyped: A
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
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
