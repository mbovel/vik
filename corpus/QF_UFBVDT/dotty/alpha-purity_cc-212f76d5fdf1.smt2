; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `purity_cc.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/purity_cc.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort IO 0)
(declare-fun x0 () (_ BitVec 32))
(declare-fun io () IO)
(declare-fun IO.now@IO (IO) (_ BitVec 32))
(assert true)
(assert (not (= x0 (IO.now@IO io))))
(check-sat)
