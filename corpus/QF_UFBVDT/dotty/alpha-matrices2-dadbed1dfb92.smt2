; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `matrices2.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/matrices2.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)101
; untyped: (param)102
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-datatypes ((Matrix 0)) (((new_Matrix (Matrix.width@Matrix (_ BitVec 32)) (Matrix.height@Matrix (_ BitVec 32))))))
(declare-fun x0 () Matrix)
(declare-fun sk_Test_180 () (_ BitVec 32))
(declare-fun sk_Test_181 () (_ BitVec 32))
(assert (bvsle (_ bv0 32) (Matrix.width@Matrix x0)))
(assert (bvsle (_ bv0 32) sk_Test_180))
(assert (bvsle (_ bv0 32) (Matrix.height@Matrix x0)))
(assert (bvsle (_ bv0 32) sk_Test_181))
(assert (bvsle (_ bv0 32) (Matrix.width@Matrix x0)))
(assert (bvsle (_ bv0 32) (Matrix.height@Matrix x0)))
(assert (and (= (Matrix.width@Matrix x0) sk_Test_180) (= (Matrix.height@Matrix x0) sk_Test_181)))
(assert (not (and (= (Matrix.width@Matrix x0) (_ bv3 32)) (= (Matrix.height@Matrix x0) (_ bv2 32)))))
(check-sat)
