; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_unfolding_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_unfolding_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: c1.T
; untyped: c2.T
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-fun x0 () (_ BitVec 32))
(declare-fun subtyping_unfolding_neg$package$.tp_c1.T () (_ BitVec 32))
(declare-fun subtyping_unfolding_neg$package$.tp_c2.T () (_ BitVec 32))
(assert (= x0 subtyping_unfolding_neg$package$.tp_c1.T))
(assert (not (= x0 subtyping_unfolding_neg$package$.tp_c2.T)))
(check-sat)
