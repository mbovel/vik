; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `maxAbstract.scala`
; original path: tests/*-custom-args/qualified-types/maxAbstract.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-fun x0 () Int)
(declare-fun quot () Int)
(declare-fun rem () Int)
(assert (= x0 (+ (* 2 quot) rem)))
(assert (or (and (>= x0 0) (>= rem 0) (< rem 2)) (and (< x0 0) (<= rem 0) (> rem (- 2)))))
(assert (= 4 x0))
(assert (= 4 x0))
(assert (= 4 x0))
(assert (not (= 0 rem)))
(check-sat)
