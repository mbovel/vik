; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `purity_cc.scala`
; original path: tests/*-custom-args/qualified-types/purity_cc.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-sort IO 0)
(declare-fun x0 () Int)
(declare-fun io () IO)
(declare-fun IO.now@IO (IO) Int)
(assert true)
(assert (not (= x0 (IO.now@IO io))))
(check-sat)
