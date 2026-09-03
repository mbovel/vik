; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `return_type_assumptions.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/return_type_assumptions.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)2
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_return_type_assumptions$package$_2 () (_ BitVec 32))
(assert (= x0 (bvadd (_ bv1 32) sk_return_type_assumptions$package$_2)))
(assert (not (= x0 (_ bv4 32))))
(check-sat)
