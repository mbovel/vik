; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `avoidance.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/avoidance.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)88
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-fun p_x () (_ BitVec 32))
(declare-fun sk_test4_37 () (_ BitVec 32))
(declare-fun sk_test4_1 () (_ BitVec 32))
(declare-fun sk_test4_3 () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(assert (bvslt p_x sk_test4_37))
(assert (bvslt sk_test4_1 sk_test4_3))
(assert (= sk_test4_37 x0))
(assert (not (= sk_test4_3 x0)))
(check-sat)
