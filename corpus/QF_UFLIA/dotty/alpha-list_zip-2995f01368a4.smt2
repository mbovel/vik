; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip.scala`
; original path: tests/*-custom-args/qualified-types/list_zip.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort scala.collection.immutable.List_B 0)
(declare-sort scala.collection.immutable.List_A 0)
(declare-fun x0 () scala.collection.immutable.List_B)
(declare-fun SeqOps.size@scala.collection.immutable.List_B (scala.collection.immutable.List_B) Int)
(declare-fun xs () scala.collection.immutable.List_A)
(declare-fun SeqOps.size@scala.collection.immutable.List_A (scala.collection.immutable.List_A) Int)
(assert (= (SeqOps.size@scala.collection.immutable.List_B x0) (SeqOps.size@scala.collection.immutable.List_A xs)))
(assert (not (= (SeqOps.size@scala.collection.immutable.List_B x0) (SeqOps.size@scala.collection.immutable.List_A xs))))
(check-sat)
