; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/BVDivision2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_BV)
(declare-fun start!3 () Bool)
(assert start!3)
(declare-fun y!1 () (_ BitVec 32))
(declare-fun x!8 () (_ BitVec 32))
(assert (=> start!3 (and (not (= y!1 #b00000000000000000000000000000000)) (= x!8 #b10000000000000000000000000000000) (= y!1 #b11111111111111111111111111111111))))
(assert (=> start!3 true))
(set-info :status sat)
(check-sat)
(get-model)
