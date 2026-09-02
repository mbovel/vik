; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_objects.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_objects.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Str 0)
(declare-sort Foo 0)
(declare-fun str () Str)
(declare-fun x0 () Foo)
(declare-fun Foo.id@Foo (Foo) Str)
(assert (= str (Foo.id@Foo x0)))
(assert (not (= str (Foo.id@Foo x0))))
(check-sat)
