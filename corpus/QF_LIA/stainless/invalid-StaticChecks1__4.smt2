; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/StaticChecks1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!17 () Bool)
(assert start!17)
(declare-fun n!0 () Int)
(declare-fun m!0 () Int)
(assert (=> start!17 (and (>= n!0 0) (>= m!0 0) (not (= m!0 0)))))
(assert (=> start!17 true))
(set-info :status sat)
(check-sat)
(get-model)
