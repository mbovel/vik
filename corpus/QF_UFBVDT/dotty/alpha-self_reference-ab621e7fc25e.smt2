; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `self_reference.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/self_reference.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)1
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort Any 0)
(declare-fun sk_selfRef_0 () Any)
(declare-fun self_reference$package$.selfRef ((_ BitVec 32)) (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(declare-fun n () (_ BitVec 32))
(assert true)
(assert true)
(assert (= (self_reference$package$.selfRef sk_selfRef_0) x0))
(assert (not (= (self_reference$package$.selfRef n) x0)))
(check-sat)
