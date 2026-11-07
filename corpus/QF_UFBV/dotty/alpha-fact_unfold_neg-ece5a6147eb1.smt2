; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `fact_unfold_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/fact_unfold_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFBV, narrowed from QF_UFBVDT, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBV)
(declare-fun fact_unfold_neg$package$.fact ((_ BitVec 32)) (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(assert (= (fact_unfold_neg$package$.fact (_ bv4294967295 32)) (bvmul (_ bv4294967295 32) (fact_unfold_neg$package$.fact (_ bv4294967294 32)))))
(assert (= (_ bv1 32) x0))
(assert (not (= (_ bv1 32) x0)))
(check-sat)
