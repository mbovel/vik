; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
(set-info :source |qualified types implication; e-graph: not proved|)
(set-info :status unknown)
(set-logic QF_UFDTLIA)
(declare-fun n () Int)
(declare-fun fib_memoized$package$.fib (Int) Int)
(declare-fun x0 () Int)
(assert (= (fib_memoized$package$.fib n) (ite (= 0 n) 0 (ite (= 1 n) 1 (+ (fib_memoized$package$.fib (+ (- 1) n)) (fib_memoized$package$.fib (+ (- 2) n)))))))
(assert (not (= 1 n)))
(assert (not (= 0 n)))
(assert true)
(assert (not (= (fib_memoized$package$.fib n) x0)))
(check-sat)
