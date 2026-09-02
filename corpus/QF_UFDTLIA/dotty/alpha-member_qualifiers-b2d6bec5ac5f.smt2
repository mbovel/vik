; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `member_qualifiers.scala`
; original path: tests/*-custom-args/qualified-types/member_qualifiers.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort Base 0)
(declare-fun t () Base)
(declare-fun Base.a@Base (Base) Int)
(declare-fun x0 () Int)
(declare-fun Base.b@Base (Base) Int)
(assert (= (Base.a@Base t) x0))
(assert (= (Base.a@Base t) x0))
(assert (< (Base.a@Base t) (Base.b@Base t)))
(assert (= (Base.a@Base t) x0))
(assert (not (< x0 (Base.b@Base t))))
(check-sat)
