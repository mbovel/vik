; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `self_reference.scala`
; original path: tests/*-custom-args/qualified-types/self_reference.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)15
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun sk_selfRef_14 () Any)
(declare-fun self_reference$package$.selfRef (Int) Int)
(declare-fun x0 () Int)
(declare-fun n () Int)
(assert true)
(assert true)
(assert (= (self_reference$package$.selfRef sk_selfRef_14) x0))
(assert (not (= (self_reference$package$.selfRef n) x0)))
(check-sat)
