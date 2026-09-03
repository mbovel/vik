; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `implicits.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/implicits.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-fun x0 () (_ BitVec 32))
(assert (= x0 (_ bv4294967254 32)))
(assert (not (bvslt x0 (_ bv0 32))))
(check-sat)
