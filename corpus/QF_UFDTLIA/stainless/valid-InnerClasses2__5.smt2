; origin: Stainless verification benchmarks, commit a1a60c98 (inox 37db9545), one solver session
; original path: frontends/benchmarks/verification/valid/InnerClasses2.scala, dumped with --solvers=smt-z3 --debug=smt --vc-cache=false --timeout=30
; license: Apache-2.0 (the stainless repository); statuses from z3
(set-logic QF_UFDTLIA)
(declare-fun start!25 () Bool)
(assert start!25)
(assert (=> start!25 false))
(declare-datatypes ((Test!5 0))(
  ( (FooBarBaz!13 (a!9 Bool) (b!15 Bool) (c!12 Bool) (l!24 Int) (m!40 Int) (o!15 Int)) (TestExt!4 (__x!6 Int)) )
))
(declare-fun lt!2 () Test!5)
(declare-fun thiss!14 () Test!5)
(assert (=> start!25 (= lt!2 thiss!14)))
(declare-fun inv!1 (Test!5) Bool)
(get-info :version)
(assert (=> start!25 (and (inv!1 thiss!14) ((_ is TestExt!4) lt!2))))
(declare-fun bs!12 () Bool)
(assert (= bs!12 start!25))
(declare-fun m!39 () Bool)
(assert (=> bs!12 m!39))
(set-info :status unsat)
(check-sat-assuming ((not start!25)))
(set-info :status unsat)
(check-sat)
