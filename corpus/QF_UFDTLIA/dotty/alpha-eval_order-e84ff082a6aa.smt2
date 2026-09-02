; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `eval_order.scala`
; original path: tests/*-custom-args/qualified-types/eval_order.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)64
; untyped: (param)65
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-sort Str 0)
(declare-fun str () Str)
(declare-fun eval_order$package$.eff (Str Int) Int)
(declare-fun x$1 () Int)
(declare-fun x$2 () Int)
(declare-fun eval_order$package$.id (Int) Int)
(declare-fun y$1 () Int)
(declare-fun str~2 () Str)
(declare-fun x0 () Int)
(declare-fun sk_Test_97 () Int)
(declare-fun sk_Test_98 () Int)
(assert (distinct str str~2))
(assert (= (eval_order$package$.eff str 1) x$1))
(assert (= (eval_order$package$.id x$2) y$1))
(assert (= x$2 (eval_order$package$.id x$2)))
(assert (= x$2 (eval_order$package$.eff str~2 2)))
(assert (= x$2 y$1))
(assert (= x$2 (eval_order$package$.eff str~2 2)))
(assert (= x0 (+ sk_Test_97 sk_Test_98)))
(assert (not (= x0 (+ x$1 y$1))))
(check-sat)
