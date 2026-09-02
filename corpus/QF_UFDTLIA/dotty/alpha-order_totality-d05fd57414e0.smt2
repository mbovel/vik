; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `order_totality.scala`
; original path: tests/*-custom-args/qualified-types/order_totality.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-fun b () Int)
(declare-fun x0 () Int)
(declare-fun c () Bool)
(declare-fun a () Int)
(assert (= b x0))
(assert (= b x0))
(assert (not c))
(assert (= c (< a b)))
(assert (= b x0))
(assert (not (<= x0 a)))
(check-sat)
