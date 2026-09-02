; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_Int_Int_Boolean 0)
(declare-datatypes ((DependentMap_Int_Int 0)) (((new_DependentMap_Int_Int (DependentMap.p@DependentMap_Int_Int scala.Function2_Int_Int_Boolean)))))
(declare-fun lambda () scala.Function2_Int_Int_Boolean)
(declare-fun x0 () DependentMap_Int_Int)
(assert (= (new_DependentMap_Int_Int lambda) x0))
(assert (not (= (new_DependentMap_Int_Int lambda) x0)))
(check-sat)
