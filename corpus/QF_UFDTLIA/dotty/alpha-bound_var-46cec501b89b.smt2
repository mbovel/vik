; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `bound_var.scala`
; original path: tests/*-custom-args/qualified-types/bound_var.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function1_Int_Int 0)
(declare-fun x0 () Int)
(declare-fun lambda () scala.Function1_Int_Int)
(declare-fun bound_var$package$.f (scala.Function1_Int_Int) Bool)
(declare-fun lambda~2 () scala.Function1_Int_Int)
(assert (and (= x0 2) (bound_var$package$.f lambda)))
(assert (not (bound_var$package$.f lambda~2)))
(check-sat)
