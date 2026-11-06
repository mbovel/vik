; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/StaticChecks3.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun n!1 () Int)
(declare-fun m!1 () Int)
(assert (=> start!1 (and (>= n!1 0) (< (+ n!1 m!1) 0))))
(assert (=> start!1 true))
(set-info :status sat)
(check-sat)
(get-model)
