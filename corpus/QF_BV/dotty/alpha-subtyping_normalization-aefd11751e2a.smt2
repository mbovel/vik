; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_normalization.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_normalization.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_BV, narrowed from QF_UFBVDT, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_BV)
(declare-fun x0 () (_ BitVec 32))
(declare-fun x () (_ BitVec 32))
(assert (= x0 (bvmul (_ bv4 32) (bvadd (_ bv1 32) x))))
(assert (not (= x0 (bvmul (_ bv4 32) (bvadd (_ bv1 32) x)))))
(check-sat)
