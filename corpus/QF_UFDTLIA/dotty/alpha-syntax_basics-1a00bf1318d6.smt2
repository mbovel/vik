; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `syntax_basics.scala`
; original path: tests/*-custom-args/qualified-types/syntax_basics.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort IntExprs 0)
(declare-fun x0 () Int)
(declare-fun this_IntExprs () IntExprs)
(declare-fun IntExprs.f@IntExprs (IntExprs Int) Int)
(assert (= x0 (IntExprs.f@IntExprs this_IntExprs x0)))
(assert (not (= x0 (IntExprs.f@IntExprs this_IntExprs x0))))
(check-sat)
