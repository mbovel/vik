; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `bijection.scala`
; original path: tests/*-custom-args/qualified-types/bijection.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort PlusMinusOne 0)
(declare-fun this_PlusMinusOne () PlusMinusOne)
(declare-fun b () Int)
(declare-fun Bijection.g@PlusMinusOne (PlusMinusOne Int) Int)
(declare-fun Bijection.f@PlusMinusOne (PlusMinusOne Int) Int)
(assert (= (Bijection.f@PlusMinusOne this_PlusMinusOne (Bijection.g@PlusMinusOne this_PlusMinusOne b)) (+ 1 (Bijection.g@PlusMinusOne this_PlusMinusOne b))))
(assert (= (Bijection.g@PlusMinusOne this_PlusMinusOne b) (+ (- 1) b)))
(assert true)
(assert (not (= b (Bijection.f@PlusMinusOne this_PlusMinusOne (Bijection.g@PlusMinusOne this_PlusMinusOne b)))))
(check-sat)
