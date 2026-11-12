; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `list_zip_2.scala`
; original path: tests/*-custom-args/qualified-types/list_zip_2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: SLists.SList[A]
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-sort Any 0)
(declare-fun xs () Any)
(declare-fun x0 () Any)
(declare-fun list_zip_2$package$.size_A (Any) Int)
(assert (= xs x0))
(assert (<= 0 (list_zip_2$package$.size_A x0)))
(assert (= xs x0))
(assert (< 0 (list_zip_2$package$.size_A xs)))
(assert (not (= 0 (list_zip_2$package$.size_A xs))))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (<= 0 (list_zip_2$package$.size_A xs)))
(assert (= xs x0))
(assert (not (< 0 (list_zip_2$package$.size_A x0))))
(check-sat)
