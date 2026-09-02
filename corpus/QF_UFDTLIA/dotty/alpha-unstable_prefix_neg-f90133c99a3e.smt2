; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `unstable_prefix_neg.scala`
; original path: tests/*-custom-args/qualified-types/unstable_prefix_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-datatypes ((test$.L 0)) (((new_test$.L))))
(declare-fun sk_foo_1 () test$.L)
(declare-fun L.rev@test$.L (test$.L) test$.L)
(declare-fun x0 () test$.L)
(declare-fun sk_foo_0 () test$.L)
(assert (= (L.rev@test$.L sk_foo_1) x0))
(assert (= x0 (L.rev@test$.L sk_foo_0)))
(assert (not false))
(check-sat)
