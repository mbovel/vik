; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses0.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!1 () Bool)
(assert start!1)
(assert (=> start!1 false))
(declare-datatypes ((Test!2 0))(
  ( (FooBarBaz!4 (a!7 Int) (l!9 Int)) (TestExt!1 (__x!3 Int)) )
))
(declare-fun thiss!19 () Test!2)
(declare-fun inv!1 (Test!2) Bool)
(assert (=> start!1 (inv!1 thiss!19)))
(declare-fun bs!1 () Bool)
(assert (= bs!1 start!1))
(declare-fun m!1 () Bool)
(assert (=> bs!1 m!1))
(set-info :status unsat)
(check-sat-assuming ((not start!1)))
(set-info :status unsat)
(check-sat)
