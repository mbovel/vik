; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `intarray.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/intarray.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-datatypes ((IntArray$.IntArray 0)) (((new_IntArray$.IntArray (IntArray.length@IntArray$.IntArray (_ BitVec 32))))))
(declare-fun x0 () IntArray$.IntArray)
(assert (bvsle (_ bv0 32) (IntArray.length@IntArray$.IntArray x0)))
(assert true)
(assert (not (= (IntArray.length@IntArray$.IntArray x0) (_ bv3 32))))
(check-sat)
