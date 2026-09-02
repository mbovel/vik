; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_scala.Int_scala.Int_scala.Boolean 0)
(declare-datatypes ((Pair_scala.Int_scala.Int 0)) (((new_Pair_scala.Int_scala.Int (Pair.p@Pair_scala.Int_scala.Int scala.Function2_scala.Int_scala.Int_scala.Boolean)))))
(declare-fun x0 () Pair_scala.Int_scala.Int)
(declare-fun lambda () scala.Function2_scala.Int_scala.Int_scala.Boolean)
(assert true)
(assert (not (= x0 (new_Pair_scala.Int_scala.Int lambda))))
(check-sat)
