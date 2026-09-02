; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized2.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)92
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun sk_fibMemo_78 () Int)
(declare-fun fib_memoized2$package$.fib (Int) Int)
(declare-fun n$3 () Int)
(declare-fun n () Int)
(declare-fun x0 () Int)
(assert (= (fib_memoized2$package$.fib sk_fibMemo_78) (ite (<= sk_fibMemo_78 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) sk_fibMemo_78)) (fib_memoized2$package$.fib (+ (- 2) sk_fibMemo_78))))))
(assert (= (fib_memoized2$package$.fib n$3) (ite (<= n$3 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) n$3)) (fib_memoized2$package$.fib (+ (- 2) n$3))))))
(assert (= n$3 (+ (- 1) n)))
(assert (< 1 n))
(assert (= (fib_memoized2$package$.fib sk_fibMemo_78) x0))
(assert (not (= (fib_memoized2$package$.fib n$3) x0)))
(check-sat)
