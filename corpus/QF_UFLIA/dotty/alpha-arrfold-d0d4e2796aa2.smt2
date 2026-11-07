; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `arrfold.scala`
; original path: tests/*-custom-args/qualified-types/arrfold.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort Foo$.IntArray 0)
(declare-fun i () Int)
(declare-fun arr () Foo$.IntArray)
(declare-fun IntArray.length@Foo$.IntArray (Foo$.IntArray) Int)
(declare-fun x0 () Int)
(assert (and (<= 0 (+ 1 i)) (< i (IntArray.length@Foo$.IntArray arr))))
(assert (<= 0 i))
(assert (<= 0 i))
(assert (<= 0 (IntArray.length@Foo$.IntArray arr)))
(assert (<= 0 x0))
(assert (not (<= 0 x0)))
(check-sat)
