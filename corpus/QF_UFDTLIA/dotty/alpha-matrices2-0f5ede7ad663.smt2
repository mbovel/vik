; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `matrices2.scala`
; original path: tests/*-custom-args/qualified-types/matrices2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort CheckedArray 0)
(declare-fun x0 () CheckedArray)
(declare-fun CheckedArray.size@CheckedArray (CheckedArray) Int)
(declare-fun size () Int)
(assert (<= 0 (CheckedArray.size@CheckedArray x0)))
(assert (<= 0 size))
(assert true)
(assert (not (= (CheckedArray.size@CheckedArray x0) size)))
(check-sat)
