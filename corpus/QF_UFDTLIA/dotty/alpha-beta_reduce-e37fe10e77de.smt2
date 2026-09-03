; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_scala.Int_scala.Int____scala.Boolean 0)
(declare-datatypes ((Outer_scala.Int 0)) (((new_Outer_scala.Int (Outer.p@Outer_scala.Int scala.Function1_scala.Int_scala.Int____scala.Boolean)))))
(declare-fun x0 () Outer_scala.Int)
(declare-fun lambda () scala.Function1_scala.Int_scala.Int____scala.Boolean)
(assert true)
(assert (not (= x0 (new_Outer_scala.Int lambda))))
(check-sat)
