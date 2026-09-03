; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `mergeSortLength.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/mergeSortLength.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)16
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort SafeSeq_scala.Int 0)
(declare-sort Any 0)
(declare-fun x0 () SafeSeq_scala.Int)
(declare-fun SafeSeq.len@SafeSeq_scala.Int (SafeSeq_scala.Int) (_ BitVec 32))
(declare-fun sk_test_0 () Any)
(declare-fun SafeSeq.len@Any (Any) (_ BitVec 32))
(assert (bvsle (_ bv0 32) (SafeSeq.len@SafeSeq_scala.Int x0)))
(assert (bvsle (_ bv0 32) (SafeSeq.len@Any sk_test_0)))
(assert (bvsle (_ bv0 32) (SafeSeq.len@SafeSeq_scala.Int x0)))
(assert (= (SafeSeq.len@SafeSeq_scala.Int x0) (SafeSeq.len@Any sk_test_0)))
(assert (not (= (SafeSeq.len@SafeSeq_scala.Int x0) (_ bv8 32))))
(check-sat)
