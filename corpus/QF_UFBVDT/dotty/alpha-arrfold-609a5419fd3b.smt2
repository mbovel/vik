; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `arrfold.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/arrfold.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)25
; untyped: (param)26
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-fun sk_$anonfun_24 () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_$anonfun_25 () (_ BitVec 32))
(assert (and (bvsle sk_$anonfun_24 x0) (bvsle sk_$anonfun_25 x0)))
(assert (not (bvsle (_ bv0 32) x0)))
(check-sat)
