; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `purity_cc.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/purity_cc.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-datatypes ((Box 0)) (((new_Box (Box.value@Box (_ BitVec 32))))))
(declare-fun x0 () Box)
(assert true)
(assert (not (= (_ bv1 32) (Box.value@Box x0))))
(check-sat)
