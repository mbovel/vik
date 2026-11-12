; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `subtyping_equality.scala`
; original path: tests/*-custom-args/qualified-types/subtyping_equality.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-fun c () Int)
(declare-fun a () Int)
(declare-fun subtyping_equality$package$.f (Int) Int)
(declare-fun d () Int)
(declare-fun b () Int)
(assert (and (and (= c (subtyping_equality$package$.f a)) (= d (subtyping_equality$package$.f b))) (= a b)))
(assert (not (= (subtyping_equality$package$.f a) (subtyping_equality$package$.f b))))
(check-sat)
