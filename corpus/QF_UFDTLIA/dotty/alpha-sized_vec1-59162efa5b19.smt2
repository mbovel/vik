; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sized_vec1.scala`
; original path: tests/*-custom-args/qualified-types/sized_vec1.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)34
; untyped: Vec
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Any 0)
(declare-fun x0 () Any)
(declare-fun sized_vec1$package$.size (Any) Int)
(declare-fun sk_Test_33 () Int)
(assert (= (sized_vec1$package$.size x0) sk_Test_33))
(assert (not (= (sized_vec1$package$.size x0) 4)))
(check-sat)
