; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerFunctions.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_LIA)
(declare-fun start!1 () Bool)
(assert start!1)
(declare-fun x!31 () Int)
(declare-fun a!21 () Int)
(assert (=> start!1 (and (>= x!31 0) (>= a!21 x!31) (< a!21 0))))
(assert (=> start!1 true))
(set-info :status unsat)
(check-sat)
