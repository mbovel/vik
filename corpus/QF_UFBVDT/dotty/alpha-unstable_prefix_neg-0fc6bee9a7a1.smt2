; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `unstable_prefix_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/unstable_prefix_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-datatypes ((test$.L 0)) (((new_test$.L))))
(declare-fun sk_foo_2 () test$.L)
(declare-fun L.rev@test$.L (test$.L) test$.L)
(declare-fun x0 () test$.L)
(declare-fun L.rev@Any (test$.L) test$.L)
(assert (= (L.rev@test$.L sk_foo_2) x0))
(assert (= x0 (L.rev@Any (new_test$.L ))))
(assert (not false))
(check-sat)
