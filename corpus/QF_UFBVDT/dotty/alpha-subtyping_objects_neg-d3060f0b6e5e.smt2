; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `subtyping_objects_neg.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/subtyping_objects_neg.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-sort Str 0)
(declare-datatypes ((PersonCaseSecondary 0)) (((new_PersonCaseSecondary (PersonCaseSecondary.name@PersonCaseSecondary Str) (PersonCaseSecondary.age@PersonCaseSecondary (_ BitVec 32))))))
(declare-fun x0 () (_ BitVec 32))
(declare-fun str () Str)
(declare-fun new_PersonCaseSecondary~2 (Str) PersonCaseSecondary)
(declare-fun PersonCaseSecondary.age@Any (PersonCaseSecondary) (_ BitVec 32))
(assert (= (_ bv0 32) x0))
(assert (not (= x0 (PersonCaseSecondary.age@Any (new_PersonCaseSecondary~2 str)))))
(check-sat)
