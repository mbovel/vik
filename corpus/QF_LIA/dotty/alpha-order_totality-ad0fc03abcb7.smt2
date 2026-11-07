; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `order_totality.scala`
; original path: tests/*-custom-args/qualified-types/order_totality.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_LIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_LIA)
(declare-fun y () Int)
(declare-fun x () Int)
(declare-fun x0 () Int)
(assert (<= y x))
(assert true)
(assert (not (and (<= x0 x) (<= x0 y))))
(check-sat)
