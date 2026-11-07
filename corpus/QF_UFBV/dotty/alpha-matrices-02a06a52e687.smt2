; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `matrices.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/matrices.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)13
; untyped: (param)12
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UFBV, narrowed from QF_UFBVDT, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBV)
(declare-sort scala.collection.immutable.List_scala.collection.immutable.List_T 0)
(declare-fun sk_transpose_3 () (_ BitVec 32))
(declare-fun sk_transpose_4 () (_ BitVec 32))
(declare-fun x0 () scala.collection.immutable.List_scala.collection.immutable.List_T)
(declare-fun matrices$package$.width_T (scala.collection.immutable.List_scala.collection.immutable.List_T) (_ BitVec 32))
(declare-fun matrices$package$.height_T (scala.collection.immutable.List_scala.collection.immutable.List_T) (_ BitVec 32))
(declare-fun m () scala.collection.immutable.List_scala.collection.immutable.List_T)
(assert (bvsle (_ bv0 32) sk_transpose_3))
(assert (bvsle (_ bv0 32) sk_transpose_4))
(assert (and (= sk_transpose_3 (matrices$package$.width_T x0)) (= sk_transpose_4 (matrices$package$.height_T x0))))
(assert (not (and (= (matrices$package$.width_T x0) (matrices$package$.height_T m)) (= (matrices$package$.height_T x0) (matrices$package$.width_T m)))))
(check-sat)
