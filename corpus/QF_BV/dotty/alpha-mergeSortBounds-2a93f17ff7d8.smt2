; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)8
; logic: QF_BV, narrowed from QF_UFBVDT, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_BV)
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_mergeSort_2 () (_ BitVec 32))
(assert (bvsle (_ bv0 32) x0))
(assert (bvsle (_ bv0 32) sk_mergeSort_2))
(assert (bvsle (_ bv0 32) x0))
(assert (bvslt x0 sk_mergeSort_2))
(assert (not (bvsle (_ bv0 32) x0)))
(check-sat)
