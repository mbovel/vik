; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_scala.Int_scala.Boolean 0)
(declare-datatypes ((Box_scala.Int 0)) (((new_Box_scala.Int (Box.p@Box_scala.Int scala.Function1_scala.Int_scala.Boolean)))))
(declare-fun beta_reduce$package$.box () Box_scala.Int)
(declare-fun lambda () scala.Function1_scala.Int_scala.Boolean)
(declare-fun x0 () (_ BitVec 32))
(declare-fun Function1.apply@scala.Function1_scala.Int_scala.Boolean (scala.Function1_scala.Int_scala.Boolean (_ BitVec 32)) Bool)
(assert (=> (= (Box.p@Box_scala.Int beta_reduce$package$.box) lambda) (= (Function1.apply@scala.Function1_scala.Int_scala.Boolean (Box.p@Box_scala.Int beta_reduce$package$.box) x0) (bvslt (_ bv0 32) x0))))
(assert (= beta_reduce$package$.box (new_Box_scala.Int lambda)))
(assert (Function1.apply@scala.Function1_scala.Int_scala.Boolean (Box.p@Box_scala.Int beta_reduce$package$.box) x0))
(assert (not (bvslt (_ bv0 32) x0)))
(check-sat)
