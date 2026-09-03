; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fact_unfold_neg.scala`
; original path: tests/*-custom-args/qualified-types/fact_unfold_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)10
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun sk_fact1_2 () Int)
(declare-fun fact_unfold_neg$package$.fact (Int) Int)
(declare-fun mul (Int Int) Int)
(declare-fun x0 () Int)
(declare-fun sk_fact1_1 () Int)
(declare-fun sk_fact1_0 () Int)
(assert (= (fact_unfold_neg$package$.fact (+ (- 1) sk_fact1_2)) (ite (= 0 (+ (- 1) sk_fact1_2)) 1 (mul (+ (- 1) sk_fact1_2) (fact_unfold_neg$package$.fact (+ (- 2) sk_fact1_2))))))
(assert (= x0 (ite (= 0 sk_fact1_0) 1 (mul (fact_unfold_neg$package$.fact (+ (- 1) sk_fact1_2)) sk_fact1_1))))
(assert (not (= 1 x0)))
(check-sat)
