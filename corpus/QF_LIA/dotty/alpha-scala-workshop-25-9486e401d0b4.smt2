; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `scala-workshop-25.scala`
; original path: tests/*-custom-args/qualified-types/scala-workshop-25.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_LIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_LIA)
(declare-fun x0 () Int)
(declare-fun x () Int)
(declare-fun y () Int)
(assert (= x0 (+ x (* 3 y))))
(assert (not (= x0 (+ x (* 3 y)))))
(check-sat)
