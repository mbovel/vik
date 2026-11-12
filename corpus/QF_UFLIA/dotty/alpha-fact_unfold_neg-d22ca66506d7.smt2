; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fact_unfold_neg.scala`
; original path: tests/*-custom-args/qualified-types/fact_unfold_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFLIA)
(declare-fun fact_unfold_neg$package$.fact (Int) Int)
(declare-fun x0 () Int)
(assert (= (fact_unfold_neg$package$.fact 1) (fact_unfold_neg$package$.fact 0)))
(assert (= x0 (* 2 (fact_unfold_neg$package$.fact 1))))
(assert (not (= 2 x0)))
(check-sat)
