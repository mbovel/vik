; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
; untyped: (param)31
; untyped: (param)32
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_Int_Int____Boolean 0)
(declare-sort scala.Function1_Int_Boolean 0)
(declare-sort Any 0)
(declare-datatypes ((Outer_Int 0)) (((new_Outer_Int (Outer.p@Outer_Int scala.Function1_Int_Int____Boolean)))))
(declare-fun beta_reduce$package$.outer () Outer_Int)
(declare-fun lambda () scala.Function1_Int_Int____Boolean)
(declare-fun x0 () Bool)
(declare-fun sk_beta_reduce$package$_30 () Any)
(declare-fun Function1.apply@scala.Function1_Int_Int____Boolean (scala.Function1_Int_Int____Boolean Any) Any)
(declare-fun sk_beta_reduce$package$_31 () Any)
(declare-fun Function1.apply@Any (Any Any) Bool)
(declare-fun lambda~2 () scala.Function1_Int_Boolean)
(assert (=> (= (Outer.p@Outer_Int beta_reduce$package$.outer) lambda) (= (Function1.apply@scala.Function1_Int_Int____Boolean (Outer.p@Outer_Int beta_reduce$package$.outer) sk_beta_reduce$package$_30) lambda~2)))
(assert (= beta_reduce$package$.outer (new_Outer_Int lambda)))
(assert (= x0 (Function1.apply@Any (Function1.apply@scala.Function1_Int_Int____Boolean (Outer.p@Outer_Int beta_reduce$package$.outer) sk_beta_reduce$package$_30) sk_beta_reduce$package$_31)))
(assert (not (= true x0)))
(check-sat)
