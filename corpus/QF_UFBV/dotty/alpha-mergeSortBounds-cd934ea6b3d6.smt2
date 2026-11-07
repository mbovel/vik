; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `mergeSortBounds.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/mergeSortBounds.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)8
; untyped: SafeSeqs.SafeSeq[T]
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UFBV, narrowed from QF_UFBVDT, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBV)
(declare-sort Any 0)
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_mergeSort_2 () (_ BitVec 32))
(declare-fun list () Any)
(declare-fun SafeSeqs$.len_T (Any) (_ BitVec 32))
(declare-fun len () (_ BitVec 32))
(assert (bvsle (_ bv0 32) x0))
(assert (bvsle (_ bv0 32) sk_mergeSort_2))
(assert (bvsle (_ bv0 32) x0))
(assert (= (SafeSeqs$.len_T list) len))
(assert (bvsle (_ bv0 32) len))
(assert (bvslt x0 sk_mergeSort_2))
(assert (not (bvslt x0 len)))
(check-sat)
