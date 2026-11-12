; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/StaticChecks1.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!41 () Bool)
(assert start!41)
(declare-fun n!3 () Int)
(declare-fun m!45 () Int)
(assert (=> start!41 (and (>= n!3 0) (>= m!45 0) (not (= m!45 0)) (< (- m!45 2) 0))))
(assert (=> start!41 true))
(set-info :status sat)
(check-sat)
(get-model)
