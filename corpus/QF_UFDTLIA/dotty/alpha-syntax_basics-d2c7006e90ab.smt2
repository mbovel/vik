; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `syntax_basics.scala`
; original path: tests/*-custom-args/qualified-types/syntax_basics.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort BoolExprs 0)
(declare-fun this_BoolExprs () BoolExprs)
(declare-fun BoolExprs.c@BoolExprs (BoolExprs) Bool)
(assert (not (BoolExprs.c@BoolExprs this_BoolExprs)))
(assert (not (not (BoolExprs.c@BoolExprs this_BoolExprs))))
(check-sat)
