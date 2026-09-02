; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized2.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_Int_Int_Boolean 0)
(declare-datatypes ((DepMap_Int_Int 0)) (((new_DepMap_Int_Int (DepMap.p@DepMap_Int_Int scala.Function2_Int_Int_Boolean)))))
(declare-fun x0 () DepMap_Int_Int)
(declare-fun lambda () scala.Function2_Int_Int_Boolean)
(assert true)
(assert (not (= x0 (new_DepMap_Int_Int lambda))))
(check-sat)
