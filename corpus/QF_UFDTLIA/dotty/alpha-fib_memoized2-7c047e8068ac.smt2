; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized2.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: (param)87
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun sk_fibMemo_76 () Int)
(declare-fun fib_memoized2$package$.fib (Int) Int)
(declare-fun n$4 () Int)
(declare-fun n () Int)
(declare-fun x0 () Int)
(assert (= (fib_memoized2$package$.fib sk_fibMemo_76) (ite (<= sk_fibMemo_76 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) sk_fibMemo_76)) (fib_memoized2$package$.fib (+ (- 2) sk_fibMemo_76))))))
(assert (= (fib_memoized2$package$.fib n$4) (ite (<= n$4 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) n$4)) (fib_memoized2$package$.fib (+ (- 2) n$4))))))
(assert (= n$4 (+ (- 2) n)))
(assert (< 1 n))
(assert (= (fib_memoized2$package$.fib sk_fibMemo_76) x0))
(assert (not (= (fib_memoized2$package$.fib n$4) x0)))
(check-sat)
