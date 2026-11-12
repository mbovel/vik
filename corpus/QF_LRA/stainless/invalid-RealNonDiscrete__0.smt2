; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/RealNonDiscrete.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LRA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun x!8 () Real)
(assert (=> start!1 (and (> x!8 (/ 1 1)) (< x!8 (/ 3 1)) (not (= x!8 (/ 2 1))))))
(assert (=> start!1 true))
(set-info :status sat)
(check-sat)
(get-model)
