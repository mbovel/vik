; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `purity_cc_sepcheck.scala`
; original path: tests/*-custom-args/qualified-types/purity_cc_sepcheck.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFLIA)
(declare-sort Cell 0)
(declare-fun c () Cell)
(declare-fun Cell.field@Cell (Cell) Int)
(declare-fun snapshot () Int)
(declare-fun x0 () Int)
(declare-fun purity_cc_sepcheck$package$.pure (Int) Int)
(assert (= (Cell.field@Cell c) snapshot))
(assert true)
(assert (not (= x0 (purity_cc_sepcheck$package$.pure snapshot))))
(check-sat)
