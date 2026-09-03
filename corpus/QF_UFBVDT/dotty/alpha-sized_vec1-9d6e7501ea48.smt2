; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `sized_vec1.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/sized_vec1.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec
; untyped: (param)55
; untyped: (param)56
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec1$package$.size (Any) (_ BitVec 32))
(declare-fun sk_Test_54 () Any)
(declare-fun sk_Test_55 () Any)
(assert (= (sized_vec1$package$.size x0) (bvadd (sized_vec1$package$.size sk_Test_54) (sized_vec1$package$.size sk_Test_55))))
(assert (not (= (sized_vec1$package$.size x0) (_ bv7 32))))
(check-sat)
