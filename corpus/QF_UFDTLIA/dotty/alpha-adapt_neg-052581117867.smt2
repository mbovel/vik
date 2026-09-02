; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `adapt_neg.scala`
; original path: tests/*-custom-args/qualified-types/adapt_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun x () Int)
(declare-fun g (Int) Int)
(declare-fun x0 () Int)
(declare-fun adapt_neg$package$.f (Int) Int)
(assert (= (g x) x0))
(assert (not (= x0 (adapt_neg$package$.f x))))
(check-sat)
