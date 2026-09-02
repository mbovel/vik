; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `range.scala`
; original path: tests/*-custom-args/qualified-types/range.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-datatypes ((CheckedRange 0)) (((new_CheckedRange (CheckedRange.from@CheckedRange Int) (CheckedRange.until@CheckedRange Int)))))
(declare-fun r () CheckedRange)
(declare-fun x0 () Int)
(assert (= (new_CheckedRange 0 10) r))
(assert (< (CheckedRange.from@CheckedRange r) (CheckedRange.until@CheckedRange r)))
(assert (= (new_CheckedRange 0 10) r))
(assert (and (<= (CheckedRange.from@CheckedRange r) x0) (< x0 (CheckedRange.until@CheckedRange r))))
(assert (not (<= 0 x0)))
(check-sat)
