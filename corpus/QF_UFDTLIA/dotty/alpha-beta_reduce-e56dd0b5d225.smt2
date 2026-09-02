; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
; untyped: (param)21
; untyped: (param)22
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_scala.Int_scala.Int_scala.Boolean 0)
(declare-sort Any 0)
(declare-datatypes ((Pair_scala.Int_scala.Int 0)) (((new_Pair_scala.Int_scala.Int (Pair.p@Pair_scala.Int_scala.Int scala.Function2_scala.Int_scala.Int_scala.Boolean)))))
(declare-fun beta_reduce$package$.pair () Pair_scala.Int_scala.Int)
(declare-fun lambda () scala.Function2_scala.Int_scala.Int_scala.Boolean)
(declare-fun x0 () Bool)
(declare-fun sk_beta_reduce$package$_20 () Int)
(declare-fun sk_beta_reduce$package$_21 () Int)
(declare-fun Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (scala.Function2_scala.Int_scala.Int_scala.Boolean Any Any) Bool)
(assert (=> (= (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) lambda) (= (Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) sk_beta_reduce$package$_20 sk_beta_reduce$package$_21) (< sk_beta_reduce$package$_20 sk_beta_reduce$package$_21))))
(assert (= beta_reduce$package$.pair (new_Pair_scala.Int_scala.Int lambda)))
(assert (= x0 (Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) sk_beta_reduce$package$_20 sk_beta_reduce$package$_21)))
(assert (not (= true x0)))
(check-sat)
