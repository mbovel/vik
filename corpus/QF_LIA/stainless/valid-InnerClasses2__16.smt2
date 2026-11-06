; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!47 () Bool)
(assert start!47)
(declare-fun l!13 () Int)
(declare-fun m!12 () Int)
(assert (=> start!47 false))
(assert (=> start!47 true))
(set-info :status unsat)
(check-sat)
