; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized2.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun n () Int)
(declare-fun fib_memoized2$package$.fib (Int) Int)
(declare-fun x0 () Int)
(assert (= (fib_memoized2$package$.fib (+ (- 1) n)) (ite (<= (+ (- 1) n) 1) 1 (+ (fib_memoized2$package$.fib (+ (- 2) n)) (fib_memoized2$package$.fib (+ (- 3) n))))))
(assert (= (fib_memoized2$package$.fib (+ (- 2) n)) (ite (<= (+ (- 2) n) 1) 1 (+ (fib_memoized2$package$.fib (+ (- 3) n)) (fib_memoized2$package$.fib (+ (- 4) n))))))
(assert (< 1 n))
(assert true)
(assert (not (= x0 (ite (<= n 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) n)) (fib_memoized2$package$.fib (+ (- 2) n)))))))
(check-sat)
