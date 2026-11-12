; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `hofsafety2.scala`
; original path: tests/*-custom-args/qualified-types/hofsafety2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_LIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_LIA)
(declare-fun x0 () Int)
(declare-fun x () Int)
(declare-fun y () Int)
(assert (= 2147483647 x0))
(assert (= 2147483647 x0))
(assert (<= (+ x y) 0))
(assert (<= 0 x))
(assert (<= 0 y))
(assert (= 2147483647 x0))
(assert (not (<= 0 x0)))
(check-sat)
