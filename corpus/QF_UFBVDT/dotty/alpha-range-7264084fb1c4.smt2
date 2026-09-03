; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `range.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/range.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-datatypes ((CheckedRange 0)) (((new_CheckedRange (CheckedRange.from@CheckedRange (_ BitVec 32)) (CheckedRange.until@CheckedRange (_ BitVec 32))))))
(declare-fun this_CheckedRange () CheckedRange)
(declare-fun x0 () (_ BitVec 32))
(assert (bvslt (CheckedRange.from@CheckedRange this_CheckedRange) (CheckedRange.until@CheckedRange this_CheckedRange)))
(assert true)
(assert (not (and (bvsle (CheckedRange.from@CheckedRange this_CheckedRange) x0) (bvslt x0 (CheckedRange.until@CheckedRange this_CheckedRange)))))
(check-sat)
