; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_scala.Int_scala.Int_scala.Boolean 0)
(declare-datatypes ((Pair_scala.Int_scala.Int 0)) (((new_Pair_scala.Int_scala.Int (Pair.p@Pair_scala.Int_scala.Int scala.Function2_scala.Int_scala.Int_scala.Boolean)))))
(declare-fun beta_reduce$package$.pair () Pair_scala.Int_scala.Int)
(declare-fun lambda () scala.Function2_scala.Int_scala.Int_scala.Boolean)
(declare-fun x0 () Bool)
(declare-fun Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (scala.Function2_scala.Int_scala.Int_scala.Boolean Int Int) Bool)
(assert (=> (= (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) lambda) (= (Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) 1 2) (< 1 2))))
(assert (= beta_reduce$package$.pair (new_Pair_scala.Int_scala.Int lambda)))
(assert (= x0 (Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) 1 2)))
(assert (not (= true x0)))
(check-sat)
