; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/BitVectorBigIntConversions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-fun x!12 () (_ BitVec 64))
(assert (=> start!17 (not (= (>= (ite (bvslt x!12 #b0000000000000000000000000000000000000000000000000000000000000000) (- (bv2nat x!12) 18446744073709551616) (bv2nat x!12)) 0) (bvsge x!12 #b0000000000000000000000000000000000000000000000000000000000000000)))))
(assert (=> start!17 true))
(set-info :status unsat)
(check-sat)
