; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `binary_search_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/binary_search_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-fun x () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(assert (bvslt (_ bv0 32) x))
(assert true)
(assert (not (and (bvsle (_ bv4294967295 32) x0) (bvslt x0 x))))
(check-sat)
