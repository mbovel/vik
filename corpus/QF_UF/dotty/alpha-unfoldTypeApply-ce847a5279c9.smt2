; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `unfoldTypeApply.scala`
; original path: tests/*-custom-args/qualified-types/unfoldTypeApply.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UF, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UF)
(declare-sort scala.collection.immutable.List_A 0)
(declare-sort scala.collection.immutable.Nil$ 0)
(declare-fun x0 () Bool)
(declare-fun l () scala.collection.immutable.List_A)
(declare-fun package$.Nil () scala.collection.immutable.Nil$)
(declare-fun Any.__@scala.collection.immutable.List_A (scala.collection.immutable.List_A scala.collection.immutable.Nil$) Bool)
(assert true)
(assert (not (= x0 (Any.__@scala.collection.immutable.List_A l package$.Nil))))
(check-sat)
