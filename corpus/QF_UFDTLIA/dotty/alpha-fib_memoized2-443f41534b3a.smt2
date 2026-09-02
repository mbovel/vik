; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized2.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized2.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_Int_Int_Boolean 0)
(declare-datatypes ((DepMap_Int_Int 0)) (((new_DepMap_Int_Int (DepMap.p@DepMap_Int_Int scala.Function2_Int_Int_Boolean)))))
(declare-fun fib_memoized2$package$.cache () DepMap_Int_Int)
(declare-fun lambda () scala.Function2_Int_Int_Boolean)
(declare-fun n () Int)
(declare-fun fib_memoized2$package$.fib (Int) Int)
(declare-fun x0 () Int)
(declare-fun Function2.apply@scala.Function2_Int_Int_Boolean (scala.Function2_Int_Int_Boolean Int Int) Bool)
(assert (=> (= (DepMap.p@DepMap_Int_Int fib_memoized2$package$.cache) lambda) (= (Function2.apply@scala.Function2_Int_Int_Boolean (DepMap.p@DepMap_Int_Int fib_memoized2$package$.cache) n x0) (= x0 (fib_memoized2$package$.fib n)))))
(assert (= fib_memoized2$package$.cache (new_DepMap_Int_Int lambda)))
(assert (= (fib_memoized2$package$.fib n) (ite (<= n 1) 1 (+ (fib_memoized2$package$.fib (+ (- 1) n)) (fib_memoized2$package$.fib (+ (- 2) n))))))
(assert (Function2.apply@scala.Function2_Int_Int_Boolean (DepMap.p@DepMap_Int_Int fib_memoized2$package$.cache) n x0))
(assert (not (= (fib_memoized2$package$.fib n) x0)))
(check-sat)
