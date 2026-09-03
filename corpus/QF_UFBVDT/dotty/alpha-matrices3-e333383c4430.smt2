; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `matrices3.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/matrices3.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-datatypes ((Matrix 0)) (((new_Matrix (Matrix.width@Matrix (_ BitVec 32)) (Matrix.height@Matrix (_ BitVec 32))))))
(declare-fun x0 () Matrix)
(declare-fun m1 () Matrix)
(assert (bvsle (_ bv0 32) (Matrix.height@Matrix x0)))
(assert (bvsle (_ bv0 32) (Matrix.width@Matrix m1)))
(assert (= m1 (new_Matrix (_ bv2 32) (_ bv3 32))))
(assert true)
(assert (not (= (Matrix.height@Matrix x0) (Matrix.width@Matrix m1))))
(check-sat)
