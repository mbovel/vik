; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `syntax_basics.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/syntax_basics.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort BoolExprs 0)
(declare-fun this_BoolExprs () BoolExprs)
(declare-fun BoolExprs.c@BoolExprs (BoolExprs) Bool)
(assert (BoolExprs.c@BoolExprs this_BoolExprs))
(assert (not (BoolExprs.c@BoolExprs this_BoolExprs)))
(check-sat)
