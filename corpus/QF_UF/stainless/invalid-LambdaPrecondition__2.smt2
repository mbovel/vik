; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/invalid/LambdaPrecondition.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UF)
(declare-fun start!9 () Bool)
(assert start!9)
(declare-fun b!1 () Bool)
(assert (=> start!9 b!1))
(assert (=> start!9 true))
(set-info :status sat)
(check-sat)
(get-model)
