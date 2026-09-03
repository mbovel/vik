; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `scala-workshop-25.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/scala-workshop-25.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-datatypes ((scala-workshop-25$package$._$Box 0)) (((new_scala-workshop-25$package$._$Box (Box.value@scala-workshop-25$package$._$Box (_ BitVec 32))))))
(declare-fun x0 () scala-workshop-25$package$._$Box)
(assert true)
(assert (not (= x0 (new_scala-workshop-25$package$._$Box (_ bv3 32)))))
(check-sat)
