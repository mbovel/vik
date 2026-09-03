; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; statuses: the `unknown` ones answered by Z3 version 4.14.0 (statusstamp, 2026-09-03)
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status sat)
(set-logic QF_UFDTLIA)
(declare-fun n () Int)
(declare-fun fib_memoized$package$.fib (Int) Int)
(declare-fun x0 () Int)
(assert (= (fib_memoized$package$.fib (+ (- 1) n)) (ite (= 0 (+ (- 1) n)) 0 (ite (= 1 (+ (- 1) n)) 1 (+ (fib_memoized$package$.fib (+ (- 2) n)) (fib_memoized$package$.fib (+ (- 3) n)))))))
(assert (= (fib_memoized$package$.fib (+ (- 2) n)) (ite (= 0 (+ (- 2) n)) 0 (ite (= 1 (+ (- 2) n)) 1 (+ (fib_memoized$package$.fib (+ (- 3) n)) (fib_memoized$package$.fib (+ (- 4) n)))))))
(assert (= (fib_memoized$package$.fib (+ (- 1) n)) (ite (= 0 (+ (- 1) n)) 0 (ite (= 1 (+ (- 1) n)) 1 (+ (fib_memoized$package$.fib (+ (- 2) n)) (fib_memoized$package$.fib (+ (- 3) n)))))))
(assert (= (fib_memoized$package$.fib (+ (- 2) n)) (ite (= 0 (+ (- 2) n)) 0 (ite (= 1 (+ (- 2) n)) 1 (+ (fib_memoized$package$.fib (+ (- 3) n)) (fib_memoized$package$.fib (+ (- 4) n)))))))
(assert (not (= 0 n)))
(assert (= (+ (fib_memoized$package$.fib (+ (- 1) n)) (fib_memoized$package$.fib (+ (- 2) n))) x0))
(assert (not (= x0 (ite (= 0 n) 0 (ite (= 1 n) 1 (+ (fib_memoized$package$.fib (+ (- 1) n)) (fib_memoized$package$.fib (+ (- 2) n))))))))
(check-sat)
