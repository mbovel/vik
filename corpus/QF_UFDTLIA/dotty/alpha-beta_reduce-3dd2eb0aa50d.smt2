; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `beta_reduce.scala`
; original path: tests/*-custom-args/qualified-types/beta_reduce.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_Int_Int____Boolean 0)
(declare-datatypes ((Outer_Int 0)) (((new_Outer_Int (Outer.p@Outer_Int scala.Function1_Int_Int____Boolean)))))
(declare-fun lambda () scala.Function1_Int_Int____Boolean)
(declare-fun x0 () Outer_Int)
(assert (= (new_Outer_Int lambda) x0))
(assert (not (= (new_Outer_Int lambda) x0)))
(check-sat)
