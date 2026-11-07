; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized2.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; logic: QF_UFLIA, narrowed from QF_UFDTLIA, which claims more than the script uses (prune logic)
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFLIA)
(declare-fun n () Int)
(declare-fun fib_memoized2$package$.fib (Int) Int)
(declare-fun x0 () Int)
(assert (= (fib_memoized2$package$.fib n) (ite (<= n 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) n)) (fib_memoized2$package$.fib (+ (- 2) n))))))
(assert (= (fib_memoized2$package$.fib n) (ite (<= n 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) n)) (fib_memoized2$package$.fib (+ (- 2) n))))))
(assert (= (fib_memoized2$package$.fib n) x0))
(assert (not (= (fib_memoized2$package$.fib n) x0)))
(check-sat)
