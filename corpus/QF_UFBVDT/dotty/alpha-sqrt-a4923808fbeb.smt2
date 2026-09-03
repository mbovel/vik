; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `sqrt.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/sqrt.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)11
; untyped: (param)12
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-fun sk_Sqrt$_11 () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_Sqrt$_12 () (_ BitVec 32))
(declare-fun Sqrt$.u () (_ BitVec 32))
(assert (and (bvsle sk_Sqrt$_11 x0) (bvsle sk_Sqrt$_12 x0)))
(assert (not (and (bvsle (_ bv0 32) x0) (bvsle Sqrt$.u x0))))
(check-sat)
