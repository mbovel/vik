; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `receiver_skolems.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/receiver_skolems.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)148
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort Counter 0)
(declare-fun x0 () Counter)
(declare-fun Counter.max@Counter (Counter) (_ BitVec 32))
(declare-fun sk_tests_157 () (_ BitVec 32))
(assert (= (Counter.max@Counter x0) sk_tests_157))
(assert (not (= (Counter.max@Counter x0) (_ bv10 32))))
(check-sat)
