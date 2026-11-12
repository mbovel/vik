; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `sized_vec1.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/sized_vec1.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: Vec
; untyped: (param)7
; untyped: (param)8
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UFBV, narrowed from QF_UFBVDT, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBV)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec1$package$.size (Any) (_ BitVec 32))
(declare-fun sk_Test_6 () Any)
(declare-fun sk_Test_7 () Any)
(assert (= (sized_vec1$package$.size x0) (bvadd (sized_vec1$package$.size sk_Test_6) (sized_vec1$package$.size sk_Test_7))))
(assert (not (= (sized_vec1$package$.size x0) (_ bv7 32))))
(check-sat)
