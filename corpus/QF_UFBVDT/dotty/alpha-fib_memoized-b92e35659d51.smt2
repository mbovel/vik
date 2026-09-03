; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `fib_memoized.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/fib_memoized.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort scala.Function2_scala.Int_scala.Int_scala.Boolean 0)
(declare-datatypes ((DependentMap_scala.Int_scala.Int 0)) (((new_DependentMap_scala.Int_scala.Int (DependentMap.p@DependentMap_scala.Int_scala.Int scala.Function2_scala.Int_scala.Int_scala.Boolean)))))
(declare-fun x0 () DependentMap_scala.Int_scala.Int)
(declare-fun lambda () scala.Function2_scala.Int_scala.Int_scala.Boolean)
(assert true)
(assert (not (= x0 (new_DependentMap_scala.Int_scala.Int lambda))))
(check-sat)
