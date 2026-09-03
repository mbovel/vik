; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `sized_vec3.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/sized_vec3.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: sized_vec3$package.Vec[String]
; untyped: sized_vec3$package.Vec[(String, scala.Int)]
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort Any 0)
(declare-fun sk_example3_8 () Any)
(declare-fun sized_vec3$package$.len_java.lang.String (Any) (_ BitVec 32))
(declare-fun mPlusN () (_ BitVec 32))
(declare-fun m () (_ BitVec 32))
(declare-fun n () (_ BitVec 32))
(declare-fun x0 () Any)
(declare-fun sized_vec3$package$.len_scala.Tuple2_java.lang.String_scala.Int (Any) (_ BitVec 32))
(assert (= (sized_vec3$package$.len_java.lang.String sk_example3_8) mPlusN))
(assert (= mPlusN (bvadd m n)))
(assert (= (sized_vec3$package$.len_java.lang.String sk_example3_8) (sized_vec3$package$.len_scala.Tuple2_java.lang.String_scala.Int x0)))
(assert (not (= (bvadd m n) (sized_vec3$package$.len_scala.Tuple2_java.lang.String_scala.Int x0))))
(check-sat)
