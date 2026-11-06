; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/BitVectorBigIntConversions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun x!8 () (_ BitVec 32))
(assert (=> start!1 (< (ite (bvslt x!8 #b00000000000000000000000000000000) (- (bv2nat x!8) 4294967296) (bv2nat x!8)) 0)))
(assert (=> start!1 true))
(set-info :status sat)
(check-sat)
(get-model)
