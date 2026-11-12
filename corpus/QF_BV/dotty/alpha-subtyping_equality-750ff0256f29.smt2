; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_equality.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_equality.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_BV, narrowed from QF_UFBVDT, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_BV)
(declare-fun x0 () (_ BitVec 32))
(declare-fun a () (_ BitVec 32))
(assert (and (= x0 a) (bvslt (_ bv3 32) a)))
(assert (not (bvslt (_ bv3 32) x0)))
(check-sat)
