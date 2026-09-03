; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_scala.Int_scala.Boolean 0)
(declare-datatypes ((Refined_scala.Int 0)) (((new_Refined_scala.Int (Refined.p@Refined_scala.Int scala.Function1_scala.Int_scala.Boolean)))))
(declare-fun x0 () Refined_scala.Int)
(declare-fun lambda () scala.Function1_scala.Int_scala.Boolean)
(assert true)
(assert (not (= x0 (new_Refined_scala.Int lambda))))
(check-sat)
