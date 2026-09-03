; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `sized_vec2.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/sized_vec2.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec[String]
; untyped: (param)148
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec2$package$.len_java.lang.String (Any) (_ BitVec 32))
(declare-fun m () (_ BitVec 32))
(declare-fun n () (_ BitVec 32))
(declare-fun mPlusN () (_ BitVec 32))
(declare-fun sk_Test_148 () (_ BitVec 32))
(assert (bvsle (_ bv0 32) (sized_vec2$package$.len_java.lang.String x0)))
(assert (bvsle (_ bv0 32) (sized_vec2$package$.len_java.lang.String x0)))
(assert (= (bvadd m n) mPlusN))
(assert (bvsle (_ bv0 32) m))
(assert (bvsle (_ bv0 32) n))
(assert (= (sized_vec2$package$.len_java.lang.String x0) sk_Test_148))
(assert (not (= (sized_vec2$package$.len_java.lang.String x0) mPlusN)))
(check-sat)
