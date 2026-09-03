; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_objects.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_objects.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort Str 0)
(declare-sort Foo 0)
(declare-fun x0 () Foo)
(declare-fun Foo.id@Foo (Foo) Str)
(declare-fun str () Str)
(assert (= (Foo.id@Foo x0) str))
(assert (not (= (Foo.id@Foo x0) str)))
(check-sat)
