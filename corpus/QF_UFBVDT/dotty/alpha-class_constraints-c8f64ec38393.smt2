; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `class_constraints.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/class_constraints.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort Str 0)
(declare-fun x0 () Str)
(declare-fun String.length@Str (Str) (_ BitVec 32))
(assert (bvslt (_ bv2 32) (String.length@Str x0)))
(assert (not (bvslt (_ bv2 32) (String.length@Str x0))))
(check-sat)
