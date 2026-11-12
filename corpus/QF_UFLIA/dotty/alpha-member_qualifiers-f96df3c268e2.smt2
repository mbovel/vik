; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `member_qualifiers.scala`
; original path: tests/*-custom-args/qualified-types/member_qualifiers.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort Aliased 0)
(declare-fun x0 () Int)
(declare-fun e () Aliased)
(declare-fun Aliased.p@Aliased (Aliased) Int)
(assert (<= 0 x0))
(assert (<= 0 (Aliased.p@Aliased e)))
(assert (<= 0 x0))
(assert (<= 0 x0))
(assert (<= 0 (Aliased.p@Aliased e)))
(assert (< x0 (Aliased.p@Aliased e)))
(assert (not (and (<= 0 x0) (< x0 (Aliased.p@Aliased e)))))
(check-sat)
