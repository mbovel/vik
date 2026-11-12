; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `member_qualifiers_neg.scala`
; original path: tests/*-custom-args/qualified-types/member_qualifiers_neg.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFLIA)
(declare-sort SelfJustifying 0)
(declare-fun this_SelfJustifying () SelfJustifying)
(declare-fun SelfJustifying.bad@SelfJustifying (SelfJustifying) Int)
(assert true)
(assert (not (= (SelfJustifying.bad@SelfJustifying this_SelfJustifying) 42)))
(check-sat)
