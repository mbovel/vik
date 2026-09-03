; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `arrfold.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/arrfold.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)15
; untyped: (param)16
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-fun sk_$anonfun_14 () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_$anonfun_15 () (_ BitVec 32))
(assert (and (bvsle sk_$anonfun_14 x0) (bvsle sk_$anonfun_15 x0)))
(assert (not (bvsle (_ bv0 32) x0)))
(check-sat)
