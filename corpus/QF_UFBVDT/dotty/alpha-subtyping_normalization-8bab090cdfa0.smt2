; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_normalization.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_normalization.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort scala.collection.immutable.List_scala.Int 0)
(declare-fun x0 () scala.collection.immutable.List_scala.Int)
(declare-fun subtyping_normalization$package$.opaqueSize_scala.Int (scala.collection.immutable.List_scala.Int) (_ BitVec 32))
(declare-fun x () (_ BitVec 32))
(assert (= (subtyping_normalization$package$.opaqueSize_scala.Int x0) (bvmul (_ bv2 32) x)))
(assert (not (= (subtyping_normalization$package$.opaqueSize_scala.Int x0) (bvmul (_ bv2 32) x))))
(check-sat)
