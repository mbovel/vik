; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `binary_search_neg.scala`
; original path: tests/*-custom-args/qualified-types/binary_search_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort IntArray 0)
(declare-fun arr () IntArray)
(declare-fun IntArray.length@IntArray (IntArray) Int)
(declare-fun x0 () Int)
(assert (<= 0 (IntArray.length@IntArray arr)))
(assert (<= 0 (IntArray.length@IntArray arr)))
(assert (< 0 (IntArray.length@IntArray arr)))
(assert (<= 0 (IntArray.length@IntArray arr)))
(assert (and (<= (- 1) x0) (< x0 (IntArray.length@IntArray arr))))
(assert (not (and (<= (- 1) x0) (< x0 (IntArray.length@IntArray arr)))))
(check-sat)
