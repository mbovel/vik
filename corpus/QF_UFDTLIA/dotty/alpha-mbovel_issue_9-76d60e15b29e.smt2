; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `mbovel_issue_9.scala`
; original path: tests/*-custom-args/qualified-types/mbovel_issue_9.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun x$1 () Int)
(declare-fun sk_TestInt_0 () Int)
(declare-fun x0 () Int)
(assert (= 1 x$1))
(assert (= x$1 sk_TestInt_0))
(assert true)
(assert (not (< x$1 x0)))
(check-sat)
