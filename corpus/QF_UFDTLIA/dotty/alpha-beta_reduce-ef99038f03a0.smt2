; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_Int_Boolean 0)
(declare-datatypes ((Box_Int 0)) (((new_Box_Int (Box.p@Box_Int scala.Function1_Int_Boolean)))))
(declare-fun beta_reduce$package$.box () Box_Int)
(declare-fun lambda () scala.Function1_Int_Boolean)
(declare-fun x0 () Int)
(declare-fun Function1.apply@scala.Function1_Int_Boolean (scala.Function1_Int_Boolean Int) Bool)
(assert (=> (= (Box.p@Box_Int beta_reduce$package$.box) lambda) (= (Function1.apply@scala.Function1_Int_Boolean (Box.p@Box_Int beta_reduce$package$.box) x0) (< 0 x0))))
(assert (= beta_reduce$package$.box (new_Box_Int lambda)))
(assert (Function1.apply@scala.Function1_Int_Boolean (Box.p@Box_Int beta_reduce$package$.box) x0))
(assert (not (< 0 x0)))
(check-sat)
