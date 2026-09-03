; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: (param)65
; untyped: (param)66
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort scala.Function2_scala.Int_scala.Int_scala.Boolean 0)
(declare-datatypes ((Pair_scala.Int_scala.Int 0)) (((new_Pair_scala.Int_scala.Int (Pair.p@Pair_scala.Int_scala.Int scala.Function2_scala.Int_scala.Int_scala.Boolean)))))
(declare-fun beta_reduce$package$.pair () Pair_scala.Int_scala.Int)
(declare-fun lambda () scala.Function2_scala.Int_scala.Int_scala.Boolean)
(declare-fun x0 () Bool)
(declare-fun sk_beta_reduce$package$_64 () (_ BitVec 32))
(declare-fun sk_beta_reduce$package$_65 () (_ BitVec 32))
(declare-fun Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (scala.Function2_scala.Int_scala.Int_scala.Boolean (_ BitVec 32) (_ BitVec 32)) Bool)
(assert (=> (= (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) lambda) (= (Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) sk_beta_reduce$package$_64 sk_beta_reduce$package$_65) (bvslt sk_beta_reduce$package$_64 sk_beta_reduce$package$_65))))
(assert (= beta_reduce$package$.pair (new_Pair_scala.Int_scala.Int lambda)))
(assert (= x0 (Function2.apply@scala.Function2_scala.Int_scala.Int_scala.Boolean (Pair.p@Pair_scala.Int_scala.Int beta_reduce$package$.pair) sk_beta_reduce$package$_64 sk_beta_reduce$package$_65)))
(assert (not (= true x0)))
(check-sat)
