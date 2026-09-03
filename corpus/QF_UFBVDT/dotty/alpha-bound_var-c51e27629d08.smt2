; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `bound_var.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/bound_var.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_scala.Int_scala.Int 0)
(declare-fun x0 () (_ BitVec 32))
(declare-fun lambda () scala.Function1_scala.Int_scala.Int)
(declare-fun bound_var$package$.f (scala.Function1_scala.Int_scala.Int) Bool)
(declare-fun lambda~2 () scala.Function1_scala.Int_scala.Int)
(assert (and (= x0 (_ bv2 32)) (bound_var$package$.f lambda)))
(assert (not (bound_var$package$.f lambda~2)))
(check-sat)
