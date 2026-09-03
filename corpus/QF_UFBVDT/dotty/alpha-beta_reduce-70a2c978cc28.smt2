; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFBVDT)
(declare-sort scala.Function1_scala.Int_scala.Boolean 0)
(declare-datatypes ((Refined_scala.Int 0)) (((new_Refined_scala.Int (Refined.p@Refined_scala.Int scala.Function1_scala.Int_scala.Boolean)))))
(declare-fun beta_reduce$package$.r () Refined_scala.Int)
(declare-fun lambda () scala.Function1_scala.Int_scala.Boolean)
(declare-fun x0 () (_ BitVec 32))
(declare-fun Function1.apply@scala.Function1_scala.Int_scala.Boolean (scala.Function1_scala.Int_scala.Boolean (_ BitVec 32)) Bool)
(assert (=> (= (Refined.p@Refined_scala.Int beta_reduce$package$.r) lambda) (= (Function1.apply@scala.Function1_scala.Int_scala.Boolean (Refined.p@Refined_scala.Int beta_reduce$package$.r) x0) (= x0 (_ bv42 32)))))
(assert (= beta_reduce$package$.r (new_Refined_scala.Int lambda)))
(assert (Function1.apply@scala.Function1_scala.Int_scala.Boolean (Refined.p@Refined_scala.Int beta_reduce$package$.r) x0))
(assert (not (= (_ bv42 32) x0)))
(check-sat)
