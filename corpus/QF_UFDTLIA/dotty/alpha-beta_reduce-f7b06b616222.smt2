; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
; untyped: (param)17
; untyped: (param)18
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_Int_Int_Boolean 0)
(declare-sort Any 0)
(declare-datatypes ((Pair_Int_Int 0)) (((new_Pair_Int_Int (Pair.p@Pair_Int_Int scala.Function2_Int_Int_Boolean)))))
(declare-fun beta_reduce$package$.pair () Pair_Int_Int)
(declare-fun lambda () scala.Function2_Int_Int_Boolean)
(declare-fun x0 () Bool)
(declare-fun sk_beta_reduce$package$_16 () Int)
(declare-fun sk_beta_reduce$package$_17 () Int)
(declare-fun Function2.apply@scala.Function2_Int_Int_Boolean (scala.Function2_Int_Int_Boolean Any Any) Bool)
(assert (=> (= (Pair.p@Pair_Int_Int beta_reduce$package$.pair) lambda) (= (Function2.apply@scala.Function2_Int_Int_Boolean (Pair.p@Pair_Int_Int beta_reduce$package$.pair) sk_beta_reduce$package$_16 sk_beta_reduce$package$_17) (< sk_beta_reduce$package$_16 sk_beta_reduce$package$_17))))
(assert (= beta_reduce$package$.pair (new_Pair_Int_Int lambda)))
(assert (= x0 (Function2.apply@scala.Function2_Int_Int_Boolean (Pair.p@Pair_Int_Int beta_reduce$package$.pair) sk_beta_reduce$package$_16 sk_beta_reduce$package$_17)))
(assert (not (= true x0)))
(check-sat)
