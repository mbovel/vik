; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `member_qualifiers_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/member_qualifiers_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort SelfJustifying 0)
(declare-fun this_SelfJustifying () SelfJustifying)
(declare-fun SelfJustifying.bad@SelfJustifying (SelfJustifying) (_ BitVec 32))
(assert true)
(assert (not (= (SelfJustifying.bad@SelfJustifying this_SelfJustifying) (_ bv42 32))))
(check-sat)
