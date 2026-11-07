; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `sqrt.scala`
; original path: tests/*-custom-args/qualified-types/sqrt.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)3
; untyped: (param)4
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_LIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_LIA)
(declare-fun sk_Sqrt$_3 () Int)
(declare-fun x0 () Int)
(declare-fun sk_Sqrt$_4 () Int)
(declare-fun Sqrt$.u () Int)
(assert (and (<= sk_Sqrt$_3 x0) (<= sk_Sqrt$_4 x0)))
(assert (not (and (<= 0 x0) (<= Sqrt$.u x0))))
(check-sat)
