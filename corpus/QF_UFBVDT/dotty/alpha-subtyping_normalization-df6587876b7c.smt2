; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_normalization.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_normalization.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort Box 0)
(declare-fun x0 () (_ BitVec 32))
(declare-fun b () Box)
(declare-fun Box.size@Box (Box) (_ BitVec 32))
(declare-fun x () (_ BitVec 32))
(assert (and (bvslt x0 (Box.size@Box b)) (bvslt (Box.size@Box b) x)))
(assert (not (bvslt x0 x)))
(check-sat)
