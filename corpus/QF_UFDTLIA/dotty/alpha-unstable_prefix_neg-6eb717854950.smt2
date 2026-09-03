; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `unstable_prefix_neg.scala`
; original path: tests/*-custom-args/qualified-types/unstable_prefix_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort test$.P 0)
(declare-fun sk_foo_4 () test$.P)
(declare-fun P.c@test$.P (test$.P) Int)
(declare-fun x0 () Int)
(declare-fun sk_foo_3 () test$.P)
(assert (= (P.c@test$.P sk_foo_4) x0))
(assert (= x0 42))
(assert (= x0 (P.c@test$.P sk_foo_3)))
(assert (not false))
(check-sat)
