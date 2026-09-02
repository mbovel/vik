; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_scala.Int_scala.Int____scala.Boolean 0)
(declare-sort scala.Function1_scala.Int_scala.Boolean 0)
(declare-sort Any 0)
(declare-datatypes ((Outer_scala.Int 0)) (((new_Outer_scala.Int (Outer.p@Outer_scala.Int scala.Function1_scala.Int_scala.Int____scala.Boolean)))))
(declare-fun beta_reduce$package$.outer () Outer_scala.Int)
(declare-fun lambda () scala.Function1_scala.Int_scala.Int____scala.Boolean)
(declare-fun x0 () Bool)
(declare-fun Function1.apply@scala.Function1_scala.Int_scala.Int____scala.Boolean (scala.Function1_scala.Int_scala.Int____scala.Boolean Int) Any)
(declare-fun Function1.apply@Any (Any Int) Bool)
(declare-fun lambda~2 () scala.Function1_scala.Int_scala.Boolean)
(assert (=> (= (Outer.p@Outer_scala.Int beta_reduce$package$.outer) lambda) (= (Function1.apply@scala.Function1_scala.Int_scala.Int____scala.Boolean (Outer.p@Outer_scala.Int beta_reduce$package$.outer) 1) lambda~2)))
(assert (= beta_reduce$package$.outer (new_Outer_scala.Int lambda)))
(assert (= x0 (Function1.apply@Any (Function1.apply@scala.Function1_scala.Int_scala.Int____scala.Boolean (Outer.p@Outer_scala.Int beta_reduce$package$.outer) 1) 2)))
(assert (not (= true x0)))
(check-sat)
