; origin: Scala 3 qualified types (scala3-rt branch qualifiers-vik, commit e7c4e412ef), one implication checked by the qualifier solver while compiling the test `fib_memoized.scala`
; original path: tests/*-custom-args/qualified-types/fib_memoized.scala, dumped with -Yqualified-types-smt-dump
; license: Apache-2.0 (the scala3 repository); :status unsat = proved by the e-graph, unknown = not proved
; untyped: <noprefix>
; untyped: R
(set-info :source |qualified types implication; e-graph: proved|)
(set-info :status unsat)
(set-logic QF_UFDTLIA)
(declare-sort scala.Function2_Int_Int_Boolean 0)
(declare-sort scala.Option_Int_with_cache.p.apply_n__boundArg_0__ 0)
(declare-datatypes ((DependentMap_Int_Int 0)) (((new_DependentMap_Int_Int (DependentMap.p@DependentMap_Int_Int scala.Function2_Int_Int_Boolean)))))
(declare-fun fib_memoized$package$.cache () DependentMap_Int_Int)
(declare-fun lambda () scala.Function2_Int_Int_Boolean)
(declare-fun x5 () scala.Option_Int_with_cache.p.apply_n__boundArg_0__)
(declare-fun Any.$isInstanceOf$@scala.Option_Int_with_cache.p.apply_n__boundArg_0___Some_Int_with_cache.p.apply_n__boundArg_0___ (scala.Option_Int_with_cache.p.apply_n__boundArg_0__) Bool)
(declare-fun n () Int)
(declare-fun x0 () Int)
(declare-fun Function2.apply@scala.Function2_Int_Int_Boolean (scala.Function2_Int_Int_Boolean Int Int) Bool)
(declare-fun fib_memoized$package$.fib (Int) Int)
(assert (=> (= (DependentMap.p@DependentMap_Int_Int fib_memoized$package$.cache) lambda) (= (Function2.apply@scala.Function2_Int_Int_Boolean (DependentMap.p@DependentMap_Int_Int fib_memoized$package$.cache) n x0) (= x0 (fib_memoized$package$.fib n)))))
(assert (=> (= (DependentMap.p@DependentMap_Int_Int fib_memoized$package$.cache) lambda) (= (Function2.apply@scala.Function2_Int_Int_Boolean (DependentMap.p@DependentMap_Int_Int fib_memoized$package$.cache) n x0) (= x0 (fib_memoized$package$.fib n)))))
(assert (= fib_memoized$package$.cache (new_DependentMap_Int_Int lambda)))
(assert (= fib_memoized$package$.cache (new_DependentMap_Int_Int lambda)))
(assert (Any.$isInstanceOf$@scala.Option_Int_with_cache.p.apply_n__boundArg_0___Some_Int_with_cache.p.apply_n__boundArg_0___ x5))
(assert (Function2.apply@scala.Function2_Int_Int_Boolean (DependentMap.p@DependentMap_Int_Int fib_memoized$package$.cache) n x0))
(assert (not (Function2.apply@scala.Function2_Int_Int_Boolean (DependentMap.p@DependentMap_Int_Int fib_memoized$package$.cache) n x0)))
(check-sat)
