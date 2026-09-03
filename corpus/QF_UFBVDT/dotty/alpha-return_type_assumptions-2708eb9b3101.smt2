; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `return_type_assumptions.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/return_type_assumptions.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)49
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFBVDT)
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_return_type_assumptions$package$_77 () (_ BitVec 32))
(declare-fun sk_return_type_assumptions$package$_78 () (_ BitVec 32))
(assert (= x0 (bvadd sk_return_type_assumptions$package$_77 sk_return_type_assumptions$package$_78)))
(assert (not (= x0 (_ bv6 32))))
(check-sat)
