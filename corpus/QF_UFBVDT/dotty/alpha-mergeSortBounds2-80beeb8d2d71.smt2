; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `mergeSortBounds2.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/mergeSortBounds2.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SafeSeqs.SafeSeq[T]
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort Any 0)
(declare-fun left () Any)
(declare-fun x0 () Any)
(declare-fun SafeSeqs$.len_T (Any) (_ BitVec 32))
(declare-fun right () Any)
(assert (= left x0))
(assert (= left x0))
(assert (and (bvslt (_ bv0 32) (SafeSeqs$.len_T left)) (bvslt (_ bv0 32) (SafeSeqs$.len_T right))))
(assert (= left x0))
(assert (not (bvslt (_ bv0 32) (SafeSeqs$.len_T x0))))
(check-sat)
