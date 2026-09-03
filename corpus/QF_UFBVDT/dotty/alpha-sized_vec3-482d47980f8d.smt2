; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `sized_vec3.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/sized_vec3.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec[Int]
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec3$package$.len_scala.Int (Any) (_ BitVec 32))
(declare-fun m () (_ BitVec 32))
(assert (= (sized_vec3$package$.len_scala.Int x0) m))
(assert (not (= (sized_vec3$package$.len_scala.Int x0) m)))
(check-sat)
