; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `intarray.scala`
; original path: tests/*-custom-args/qualified-types/intarray.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((IntArray$.IntArray 0)) (((new_IntArray$.IntArray (IntArray.length@IntArray$.IntArray Int)))))
(declare-fun x0 () IntArray$.IntArray)
(assert (<= 0 (IntArray.length@IntArray$.IntArray x0)))
(assert (= x0 (new_IntArray$.IntArray 3)))
(assert (not (= (IntArray.length@IntArray$.IntArray x0) 3)))
(check-sat)
