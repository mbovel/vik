; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `adapt_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/adapt_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-fun x () (_ BitVec 32))
(declare-fun g ((_ BitVec 32)) (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(assert (= (g (bvadd (_ bv2 32) x)) x0))
(assert (not (= x0 (g (bvadd (_ bv1 32) x)))))
(check-sat)
