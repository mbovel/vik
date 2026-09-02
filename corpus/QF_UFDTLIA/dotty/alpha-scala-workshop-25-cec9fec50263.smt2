; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `scala-workshop-25.scala`
; original path: tests/*-custom-args/qualified-types/scala-workshop-25.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-datatypes ((scala-workshop-25$package$._$Box 0)) (((new_scala-workshop-25$package$._$Box (Box.value@scala-workshop-25$package$._$Box Int)))))
(declare-fun x0 () scala-workshop-25$package$._$Box)
(assert true)
(assert (not (= x0 (new_scala-workshop-25$package$._$Box 3))))
(check-sat)
