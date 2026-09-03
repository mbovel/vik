; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `adapt_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/adapt_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-datatypes ((IntBox 0)) (((new_IntBox (IntBox.x@IntBox (_ BitVec 32))))))
(declare-fun x () (_ BitVec 32))
(declare-fun x0 () IntBox)
(assert (= (new_IntBox x) x0))
(assert (not (= x0 (new_IntBox (bvadd (_ bv1 32) x)))))
(check-sat)
