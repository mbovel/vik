; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: (param)39
; untyped: (param)40
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean 0)
(declare-sort scala.Function1_scala.Int_scala.Boolean 0)
(declare-datatypes ((Outer_scala.Int 0)) (((new_Outer_scala.Int (Outer.p@Outer_scala.Int scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean)))))
(declare-fun beta_reduce$package$.outer () Outer_scala.Int)
(declare-fun lambda () scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean)
(declare-fun x0 () Bool)
(declare-fun sk_beta_reduce$package$_38 () (_ BitVec 32))
(declare-fun Function1.apply@scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean (scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean (_ BitVec 32)) scala.Function1_scala.Int_scala.Boolean)
(declare-fun sk_beta_reduce$package$_39 () (_ BitVec 32))
(declare-fun Function1.apply@scala.Function1_scala.Int_scala.Boolean (scala.Function1_scala.Int_scala.Boolean (_ BitVec 32)) Bool)
(declare-fun lambda~2 () scala.Function1_scala.Int_scala.Boolean)
(assert (=> (= (Outer.p@Outer_scala.Int beta_reduce$package$.outer) lambda) (= (Function1.apply@scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean (Outer.p@Outer_scala.Int beta_reduce$package$.outer) sk_beta_reduce$package$_38) lambda~2)))
(assert (=> (= (Function1.apply@scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean (Outer.p@Outer_scala.Int beta_reduce$package$.outer) sk_beta_reduce$package$_38) lambda~2) (= (Function1.apply@scala.Function1_scala.Int_scala.Boolean (Function1.apply@scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean (Outer.p@Outer_scala.Int beta_reduce$package$.outer) sk_beta_reduce$package$_38) sk_beta_reduce$package$_39) (bvslt sk_beta_reduce$package$_38 sk_beta_reduce$package$_39))))
(assert (= beta_reduce$package$.outer (new_Outer_scala.Int lambda)))
(assert (= x0 (Function1.apply@scala.Function1_scala.Int_scala.Boolean (Function1.apply@scala.Function1_scala.Int_scala.Function1_scala.Int_scala.Boolean (Outer.p@Outer_scala.Int beta_reduce$package$.outer) sk_beta_reduce$package$_38) sk_beta_reduce$package$_39)))
(assert (not (= true x0)))
(check-sat)
