; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik), one implication checked by the qualifier solver while compiling the test `eval_order.scala`, Int/Long as 32/64-bit vectors
; original path: tests/*-custom-args/qualified-types/eval_order.scala, dumped with -Yqualified-types-smt-dump (bit-vector default)
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)54
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFBVDT)
(declare-sort Str 0)
(declare-fun str () Str)
(declare-fun eval_order$package$.eff (Str (_ BitVec 32)) (_ BitVec 32))
(declare-fun x$2 () (_ BitVec 32))
(declare-fun x0 () (_ BitVec 32))
(declare-fun sk_Test_87 () (_ BitVec 32))
(assert (= (eval_order$package$.eff str (_ bv2 32)) x$2))
(assert (= x0 sk_Test_87))
(assert (not (= x$2 x0)))
(check-sat)
