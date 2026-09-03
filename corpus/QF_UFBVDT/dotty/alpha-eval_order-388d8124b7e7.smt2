; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `eval_order.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/eval_order.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)36
; untyped: (param)37
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-fun sk_Test_6 () (_ BitVec 32))
(declare-fun sk_Test_4 () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_Test_51 () (_ BitVec 32))
(declare-fun sk_Test_52 () (_ BitVec 32))
(declare-fun sk_Test_2 () (_ BitVec 32))
(assert (= sk_Test_6 sk_Test_4))
(assert (= x0 (bvadd sk_Test_51 sk_Test_52)))
(assert (not (= x0 (bvadd sk_Test_6 sk_Test_2))))
(check-sat)
