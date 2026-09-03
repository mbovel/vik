; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_unfolding.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_unfolding.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-fun x () (_ BitVec 32))
(declare-fun x2 () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(declare-fun subtyping_unfolding$package$.id_scala.Int ((_ BitVec 32)) (_ BitVec 32))
(assert (= x x2))
(assert (= x0 (subtyping_unfolding$package$.id_scala.Int x)))
(assert (not (= x0 (subtyping_unfolding$package$.id_scala.Int x2))))
(check-sat)
